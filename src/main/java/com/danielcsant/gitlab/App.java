package com.danielcsant.gitlab;

import com.danielcsant.gitlab.model.Metric;
import com.danielcsant.gitlab.model.TestCoverage;
import com.danielcsant.gitlab.repository.IMetricDao;
import com.danielcsant.gitlab.repository.MetricDaoMySqlImpl;
import com.danielcsant.gitlab.service.*;
import org.gitlab4j.api.models.Issue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class App {

    private final static Logger LOGGER = LoggerFactory.getLogger("com.danielcsant.gitlab.App");

    private static SheetsService sheetsService;

    private static CFDMetricsService gitlabService;
    private static ExpeditesMetricsService expeditesMetricsService;
    private static NewTasksMetricsService newTasksMetricsService;
    private static TestCoverageMetricsService testCoverageMetricsService;

    private static String projectName;

    public static void main(String[] args) throws Exception {
        LOGGER.info("Loading configuration");
        Properties prop = new Properties();
        try (InputStream input = App.class.getClassLoader().getResourceAsStream("app.properties")) {
            // load a properties file
            prop.load(input);
        } catch (IOException ex) {
            throw new Exception("Configure app.properties file");
        }

        LOGGER.info("Initializing services");
        String personalAccessToken = prop.getProperty("personalAccessToken");
        String hostUrl = prop.getProperty("hostUrl");
        String columnNames[] = {"Open","To Do","Doing","Desplegado en Test","Despliegue Pendiente","Desplegado","Closed"};
        String sheetId = prop.getProperty("sheetId");

        sheetsService = new SheetsService(sheetId);
        gitlabService = new CFDMetricsService(hostUrl, personalAccessToken);
        expeditesMetricsService = new ExpeditesMetricsService(hostUrl, personalAccessToken);
        newTasksMetricsService = new NewTasksMetricsService(hostUrl, personalAccessToken);
        testCoverageMetricsService = new TestCoverageMetricsService(hostUrl, personalAccessToken);

        String [] projects = prop.getProperty("projects").split(",");
        ArrayList metrics = new ArrayList();
        for (int i = 0; i < projects.length; i++) {
            String [] projectData = projects[i].split(":");
            projectName = projectData[0];
            int closedAtStart = Integer.parseInt(projectData[1]);

            LOGGER.info("Retrieving data from project: " + projectName);
            HashMap<String, List<Issue>> columns = gitlabService.getColumnsMap(projectName, columnNames);
            Metric newMetric = generateTodayMetrics(columnNames, columns, closedAtStart);
            LOGGER.info("Retrieved");

            metrics.add(newMetric);
        }

        LOGGER.info("Inserting metrics for " + metrics.size() + " projects");
        IMetricDao iMetricDao = new MetricDaoMySqlImpl();
        boolean inserted = iMetricDao.insert("project", metrics);
        if (inserted){
            LOGGER.info("Inserted");
        } else {
            LOGGER.warn("No metrics inserted");
        }

    }

    private static Metric generateTodayMetrics(String[] columnNames, HashMap<String, List<Issue>> columns, int closedAtStart) throws Exception {
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

        List<Issue> bugsCreatedYesterday = expeditesMetricsService.getExpeditesCreatedLastWorkingDay(projectName, columns);
        columnMap.put("Expedites", bugsCreatedYesterday.size());

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
                columnMap.get("Expedites"),
                columnMap.get("Coverage"),
                columnMap.get("New tasks"),
                projectName
                );

        return newMetric;
    }

}
