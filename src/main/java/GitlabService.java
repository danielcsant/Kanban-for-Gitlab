import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.models.Issue;
import org.gitlab4j.api.models.Project;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GitlabService {

    GitLabApi gitLabApi = null;
    List<Project> projects = null;

    public GitlabService(String hostUrl, String personalAccessToken) throws GitLabApiException {
        // Create a GitLabApi instance to communicate with your GitLab server
        gitLabApi = new GitLabApi(hostUrl, personalAccessToken);

        // Get the list of projects your account has access to
        projects = gitLabApi.getProjectApi().getProjects();
    }

    public HashMap<String, List<Issue>> getColumnsMap(String projectName, String[] columnNames) throws Exception {
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

        return columns;
    }
}
