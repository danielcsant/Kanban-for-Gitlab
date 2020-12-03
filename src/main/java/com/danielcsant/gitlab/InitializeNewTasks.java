package com.danielcsant.gitlab;

import com.danielcsant.gitlab.model.IssuesInDate;
import com.danielcsant.gitlab.service.CFDMetricsService;
import com.danielcsant.gitlab.service.NewTasksMetricsService;
import com.danielcsant.gitlab.service.SheetsService;
import org.gitlab4j.api.models.Issue;

import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class InitializeNewTasks {

    private static SheetsService sheetsService;
    private static CFDMetricsService gitlabService;
    private static NewTasksMetricsService newTasksMetricsService;

    private static String projectName;

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
        projectName = prop.getProperty("projectName");
        String columnNames[] = prop.getProperty("columns").split(",");
        String sheetId = prop.getProperty("sheetId");
        int closedAtStart = Integer.parseInt(prop.getProperty("closedAtStart", "0"));

        sheetsService = new SheetsService(sheetId);
        gitlabService = new CFDMetricsService(hostUrl, personalAccessToken);
        newTasksMetricsService = new NewTasksMetricsService(hostUrl, personalAccessToken);

        HashMap<String, List<Issue>> columns = gitlabService.getColumnsMap(projectName, columnNames);

        generateNewTaskMetrics(columns);
    }

    private static void generateNewTaskMetrics(HashMap<String, List<Issue>> columns) throws Exception {
        List<IssuesInDate> tasksCreatedYesterday = newTasksMetricsService.getCreatedIssues(columns);

        List<List<Object>> newRows = new ArrayList<>();

        for (IssuesInDate issuesInDate : tasksCreatedYesterday) {
            ArrayList newTaskRow = new ArrayList();
            newTaskRow.add(getFormattedDate(issuesInDate.getCreated()));
            newTaskRow.add(issuesInDate.getIssues().size());
            StringBuffer urls = new StringBuffer();
            for (Issue issue : issuesInDate.getIssues()) {
                if (urls.length() != 0){
                    urls.append("\n");
                }
                urls.append(issue.getWebUrl());
            }
            newTaskRow.add(urls.toString());
            newRows.add(newTaskRow);
        }

        sheetsService.persistNewRow("New tasks", newRows);
    }

    private static String getFormattedDate(Date date) {
        DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        return formatter.format(date);
    }

}
