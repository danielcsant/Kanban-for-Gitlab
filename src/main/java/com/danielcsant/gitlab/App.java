package com.danielcsant.gitlab;

import com.danielcsant.gitlab.model.ColumnStatus;
import com.danielcsant.gitlab.model.IssueColumnStatuses;
import com.danielcsant.gitlab.model.TestCoverage;
import com.danielcsant.gitlab.service.*;
import org.gitlab4j.api.Constants;
import org.gitlab4j.api.models.Issue;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class App {

    private final static Logger LOGGER = Logger.getLogger("com.danielcsant.gitlab.App");

    private static SheetsService sheetsService;

    private static CFDMetricsService gitlabService;
    private static BugsMetricsService bugsMetricsService;
    private static NewTasksMetricsService newTasksMetricsService;
    private static TestCoverageMetricsService testCoverageMetricsService;

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
        gitlabService = new CFDMetricsService(hostUrl, personalAccessToken, closedAtStart);
        bugsMetricsService = new BugsMetricsService(hostUrl, personalAccessToken, closedAtStart);
        newTasksMetricsService = new NewTasksMetricsService(hostUrl, personalAccessToken, closedAtStart);
        testCoverageMetricsService = new TestCoverageMetricsService(hostUrl, personalAccessToken);

        HashMap<String, List<Issue>> columns = gitlabService.getColumnsMap(projectName, columnNames);

        generateCFDmetrics(columnNames, closedAtStart, columns);
        generateBugsMetrics(columns);
        generateNewTaskMetrics(columns);
        generateTestCoverageMetrics();

//        generateLeadTimeMetrics(projectName, columnNames, columns);
//        generateCustomerLeadTimeMetrics(projectName, columnNames, columns);
    }

    private static void generateTestCoverageMetrics() throws Exception {
        TestCoverage testCoverage = testCoverageMetricsService.getTestCoverageLastWorkingDay(projectName);

        if (testCoverage != null) {
            List<List<Object>> newRows = new ArrayList<>();

            ArrayList newTaskRow = new ArrayList();
            newTaskRow.add(getFormattedDate(testCoverage.getUpdatedAt()));
            newTaskRow.add(Double.parseDouble(testCoverage.getCoverage()));
            newRows.add(newTaskRow);

            sheetsService.persistNewRow("Coverage", newRows);
        }
    }

    private static void generateNewTaskMetrics(HashMap<String, List<Issue>> columns) throws Exception {
        List<Issue> tasksCreatedYesterday = newTasksMetricsService.getTasksCreatedYesterday(columns);

        StringBuffer urls = new StringBuffer();
        for (Issue issue : tasksCreatedYesterday) {
            if (urls.length() != 0){
                urls.append("\n");
            }
            urls.append(issue.getWebUrl());
        }

        Date date = Calendar.getInstance().getTime();
        String today = getFormattedDate(date);

        ArrayList newTaskRow = new ArrayList();
        newTaskRow.add(today);
        newTaskRow.add(tasksCreatedYesterday.size());
        newTaskRow.add(urls.toString());
        List<List<Object>> newRows = new ArrayList<>();
        newRows.add(newTaskRow);

        sheetsService.persistNewRow("New tasks", newRows);
    }

    private static void generateBugsMetrics(HashMap<String, List<Issue>> columns) throws Exception {

        List<Issue> bugsCreatedYesterday = bugsMetricsService.getBugsCreatedYesterday(projectName, columns);

        StringBuffer urls = new StringBuffer();
        for (Issue issue : bugsCreatedYesterday) {
            if (urls.length() != 0){
                urls.append("\n");
            }
            urls.append(issue.getWebUrl());
        }

        Date date = Calendar.getInstance().getTime();
        String today = getFormattedDate(date);

        ArrayList bugRow = new ArrayList();
        bugRow.add(today);
        bugRow.add(bugsCreatedYesterday.size());
        bugRow.add(urls.toString());
        List<List<Object>> newRows = new ArrayList<>();
        newRows.add(bugRow);

        sheetsService.persistNewRow("Bugs", newRows);
    }

    private static void generateCustomerLeadTimeMetrics(String projectName, String[] columnNames, HashMap<String, List<Issue>> columns) throws Exception {
        List<List<Object>> newRows = new ArrayList<>();

        for (Issue issue : columns.get("Closed")) {
            ArrayList customerLeadtimeRow = new ArrayList();
            customerLeadtimeRow.add(issue.getIid());
            customerLeadtimeRow.add(getFormattedDateWithHours(issue.getCreatedAt()));
            customerLeadtimeRow.add(getFormattedDateWithHours(issue.getClosedAt()));
            customerLeadtimeRow.add(getHoursBetweenDates(issue.getCreatedAt(), issue.getClosedAt()));
            customerLeadtimeRow.add(getIssueSize(issue));
            customerLeadtimeRow.add(issue.getWebUrl());
            newRows.add(customerLeadtimeRow);
        }

        Collections.reverse(newRows);
        sheetsService.updateRows("Customer Lead Times", newRows);

    }

    private static void generateLeadTimeMetrics(String projectName, String[] columnNames, HashMap<String, List<Issue>> columns) throws Exception {

        List<IssueColumnStatuses> issuesStatuses =
                gitlabService.getIssuesStatuses(projectName, columns, columnNames);

        Date date = Calendar.getInstance().getTime();
        String today = getFormattedDateWithHours(date);

        List<List<Object>> newRows = new ArrayList<>();
        ArrayList leadtimeRow = null;

        List cycleTimeColumns = Arrays.asList("Doing", "Desplegado en Test");
        for (IssueColumnStatuses issuesStatus : issuesStatuses) {
            Issue issue = issuesStatus.getIssue();
            if (issue.getState() == Constants.IssueState.CLOSED) {
                Date startDate = getEarliestDateInIssue(issuesStatus.getColumnStatusHashMap(), cycleTimeColumns);
                Date endDate = getLatestDateInIssue(issuesStatus.getColumnStatusHashMap(), cycleTimeColumns);
                long hoursBetweenDates = 0;
                try {
                    hoursBetweenDates = getHoursBetweenDates(startDate, endDate);
                } catch (NullPointerException npe){
                    LOGGER.log(Level.SEVERE, issuesStatus.toString());
                }
                String issueSize = getIssueSize(issue);

                leadtimeRow = new ArrayList();
                leadtimeRow.add(issue.getIid());
                leadtimeRow.add(hoursBetweenDates);
                leadtimeRow.add(issueSize);
                newRows.add(leadtimeRow);

            }
        }

        sheetsService.persistNewRow("Lead times (draft)", newRows);

    }

    private static String getIssueSize(Issue issue) {
        String issueSize = "";

        List issueSizes = Arrays.asList("XS", "S", "M", "L", "XL", "XXL");
        if (!issue.getLabels().isEmpty()) {
            for (String label : issue.getLabels()) {
                if (issueSizes.contains(label)){
                    issueSize = label;
                }
            }
        }

        return issueSize;
    }

    private static long getHoursBetweenDates(Date startDate, Date endDate) throws NullPointerException {
        long secs = (endDate.getTime() - startDate.getTime()) / 1000;
        long hours = secs / 3600;

        return hours;
    }

    private static Date getLatestDateInIssue(HashMap<String, ColumnStatus> columnStatusHashMap, List cycleTimeColumns) {
        Date latestDate = null;

        for (String s : columnStatusHashMap.keySet()) {
            if (cycleTimeColumns.contains(s)){
                Date removedDate = columnStatusHashMap.get(s).getRemovedDate();
                if (removedDate != null &&
                        (latestDate == null || latestDate.before(removedDate))) {
                    latestDate = removedDate;
                }
            }
        }

        return latestDate;
    }

    private static Date getEarliestDateInIssue(HashMap<String, ColumnStatus> columnStatuses, List<String> cycleTimeColumns) {
        Date earliestDate = null;

        for (String s : columnStatuses.keySet()) {
            if (cycleTimeColumns.contains(s)){
                Date addedDate = columnStatuses.get(s).getAddedDate();
                if (addedDate!= null &&
                        (earliestDate == null || earliestDate.after(addedDate))) {
                    earliestDate = addedDate;
                }
            }
        }

        return earliestDate;
    }

    private static void generateCFDmetrics(String[] columnNames,
                                                    int closedAtStart,
                                                    HashMap<String,List<Issue>> columns) throws GeneralSecurityException, IOException {

        Date date = Calendar.getInstance().getTime();
        String today = getFormattedDate(date);

        ArrayList cfdRow = new ArrayList();
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

        List<List<Object>> newRow = new ArrayList<>();
        newRow.add(cfdRow);
        sheetsService.persistNewRow("CFD", newRow);
    }

    private static String getFormattedDateWithHours(Date date) {
        DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        return formatter.format(date);
    }

    private static String getFormattedDate(Date date) {
        DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        return formatter.format(date);
    }

}
