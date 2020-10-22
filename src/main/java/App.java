import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.models.Issue;
import org.gitlab4j.api.models.Project;

import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
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
        String sheetId = prop.getProperty("sheetId");
        int closedAtStart = Integer.parseInt(prop.getProperty("closedAtStart", "0"));

        GitlabService gitlabService = new GitlabService(hostUrl, personalAccessToken, closedAtStart);
        HashMap<String, List<Issue>> columns = null;
        columns = gitlabService.getColumnsMap(projectName, columnNames);

        for (String s : columnNames) {
            System.out.println("Issues in column " + s + ": " + columns.get(s).size());
        }

        ArrayList cfdRow = new ArrayList();
        Date date = Calendar.getInstance().getTime();

        // Display a date in day, month, year format
        DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        String today = formatter.format(date);
        cfdRow.add(today);
        for (int i = 0; i < columnNames.length; i++) {
            String columnName = columnNames[i];
            int columnSize = columns.get(columnName).size();
            if (columnName.equalsIgnoreCase("closed")){
                cfdRow.add(columnSize - closedAtStart);
            } else {
                cfdRow.add(columnSize);
            }
        }
        cfdRow.add(columns.get("Reopened").size());

        SheetsService sheetsService = new SheetsService(sheetId, columns, columnNames);
        sheetsService.persistNewRow(cfdRow);

    }

}
