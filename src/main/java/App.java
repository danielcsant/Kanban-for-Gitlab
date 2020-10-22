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

        GitlabService gitlabService = new GitlabService(hostUrl, personalAccessToken);

        HashMap<String, List<Issue>> columns = gitlabService.getColumnsMap(projectName, columnNames);

        for (String s : columnNames) {
            System.out.println("Issues in column " + s + ": " + columns.get(s).size());
        }

        SheetsService sheetsService = new SheetsService(columns, columnNames);

    }

}
