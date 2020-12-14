package com.danielcsant.gitlab;

import com.danielcsant.gitlab.model.Metric;
import com.danielcsant.gitlab.model.TestCoverage;
import com.danielcsant.gitlab.repository.IMetricDao;
import com.danielcsant.gitlab.repository.MetricDaoMySqlImpl;
import com.danielcsant.gitlab.service.*;
import org.gitlab4j.api.models.Issue;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
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
        String columnNames[] = {"Open","To Do","Doing","Desplegado en Test","Despliegue Pendiente","Desplegado","Closed"};
        String sheetId = prop.getProperty("sheetId");

        sheetsService = new SheetsService(sheetId);
        gitlabService = new CFDMetricsService(hostUrl, personalAccessToken);
        bugsMetricsService = new BugsMetricsService(hostUrl, personalAccessToken);
        newTasksMetricsService = new NewTasksMetricsService(hostUrl, personalAccessToken);
        testCoverageMetricsService = new TestCoverageMetricsService(hostUrl, personalAccessToken);

        String [] projects = prop.getProperty("projects").split(",");
        for (int i = 0; i < projects.length; i++) {
            String [] projectData = projects[i].split(":");
            projectName = projectData[0];
            int closedAtStart = Integer.parseInt(projectData[1]);

            HashMap<String, List<Issue>> columns = gitlabService.getColumnsMap(projectName, columnNames);

            // Remove when MYSQL migration is done. Now writing in excel just for first project...
            if (i == 0) {
                // Writing in excel
                generateCFDmetrics(columnNames, closedAtStart, columns);
                generateBugsMetrics(columns);
                generateNewTaskMetrics(columns);
                generateTestCoverageMetrics();
            }

            // Writing in MySQL
            generateTodayMetrics(columnNames, columns, closedAtStart);

        }
    }

    private static void generateTodayMetrics(String[] columnNames, HashMap<String, List<Issue>> columns, int closedAtStart) throws Exception {
        Date today = Calendar.getInstance().getTime();
        java.sql.Date sqlDate = new java.sql.Date(today.getTime());

        HashMap<String, Integer> columnMap = new HashMap();
        for (int i = 0; i < columnNames.length; i++) {
            String columnName = columnNames[i];
            int columnSize = columns.get(columnName).size();
            if (columnName.equalsIgnoreCase("closed")){
                columnMap.put(columnName, columnSize - closedAtStart);
            } else {
                columnMap.put(columnName, columnSize);
            }
        }

        List<Issue> bugsCreatedYesterday = bugsMetricsService.getBugsCreatedLastWorkingDay(projectName, columns);
        columnMap.put("New bugs", bugsCreatedYesterday.size());

        List<Issue> tasksCreatedYesterday = newTasksMetricsService.getTasksCreatedYesterday(columns);
        columnMap.put("New tasks", tasksCreatedYesterday.size());

        TestCoverage testCoverage = testCoverageMetricsService.getTestCoverageLastWorkingDay(projectName);
        columnMap.put("Coverage", (int) Double.parseDouble(testCoverage.getCoverage()));

        Metric newMetric = new Metric(
                sqlDate,
                columnMap.get("Open"),
                columnMap.get("To Do"),
                columnMap.get("Doing"),
                columnMap.get("Desplegado en Test"),
                columnMap.get("Despliegue Pendiente"),
                columnMap.get("Desplegado"),
                columnMap.get("Closed"),
                columnMap.get("New bugs"),
                columnMap.get("Coverage"),
                columnMap.get("New tasks")
                );

        IMetricDao iMetricDao = new MetricDaoMySqlImpl();
        iMetricDao.insert(projectName, newMetric);
    }

    private static void generateTestCoverageMetrics() throws Exception {
        TestCoverage testCoverage = testCoverageMetricsService.getTestCoverageLastWorkingDay(projectName);

        if (testCoverage != null) {
            List<List<Object>> newRows = new ArrayList<>();

            ArrayList newTaskRow = new ArrayList();
            newTaskRow.add(getFormattedDate(new Date()));
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

        List<Issue> bugsCreatedYesterday = bugsMetricsService.getBugsCreatedLastWorkingDay(projectName, columns);

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
