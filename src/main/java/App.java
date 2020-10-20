import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.models.Issue;
import org.gitlab4j.api.models.Project;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class App {



    public static void main(String[] args) throws Exception {
        Properties prop = new Properties();
        try (InputStream input = App.class.getClassLoader().getResourceAsStream("app.properties")) {
            // load a properties file
            prop.load(input);
        } catch (IOException ex) {
            throw new Exception("Configure app.properties file");
        }

        String personalAccessToken = prop.getProperty("personalAccessToken");
        String hostUrl = prop.getProperty("hostUrl");
        String projectName = prop.getProperty("projectName");
        String columnNames[] = prop.getProperty("columns").split(",");


        // Create a GitLabApi instance to communicate with your GitLab server
        GitLabApi gitLabApi = new GitLabApi(hostUrl, personalAccessToken);

        // Get the list of projects your account has access to
        List<Project> projects = gitLabApi.getProjectApi().getProjects();

        Project project = null;
        for (Project projectIter : projects) {
            if (projectIter.getName().equalsIgnoreCase(projectName)){
                project = projectIter;
                break;
            }
        }

        if (project == null){
            throw new Exception("Project not found");
        }


        HashMap<String, List<Issue>> columns = new HashMap<>();
        for (int i = 0; i < columnNames.length; i++) {
            String column = columnNames[i];
            columns.put(column, new ArrayList<>());
        }

        List<Issue> openedIssues = new ArrayList<>();
        List<Issue> reopenedIssues = new ArrayList<>();
        List<Issue> closedIssues = new ArrayList<>();
        // Get a list of issues for the specified project ID
        List<Issue> issues = gitLabApi.getIssuesApi().getIssues(project.getId());
        for (Issue issue : issues) {
            switch (issue.getState()) {
                case OPENED:
                    openedIssues.add(issue);
                    break;
                case CLOSED:
                    closedIssues.add(issue);
                    break;
                case REOPENED:
                    reopenedIssues.add(issue);
                    break;
            }

            switch (issue.getState()) {
                case OPENED:
                case REOPENED:
                    try {
                        if (issue.getLabels() != null && issue.getLabels().size() > 0){
                            for (String label : issue.getLabels()) {
                                if (columns.containsKey(label)){
                                    columns.get(label).add(issue);
                                }
                            }
                        }
                    } catch (Exception e){
                        if (issue.getLabels() != null && issue.getLabels().size() > 0){
                            System.err.println(issue.getLabels());
                        }
                        throw e;
                    }

                    break;
            }

        }

        for (String s : columnNames) {
            System.out.println("Issues in column " + s + ": " + columns.get(s).size());
        }

    }

}
