package com.danielcsant.gitlab;

import com.danielcsant.gitlab.model.ProjectMetric;
import com.danielcsant.gitlab.model.TeamMetric;
import com.danielcsant.gitlab.model.TestCoverage;
import com.danielcsant.gitlab.repository.IProjectDao;
import com.danielcsant.gitlab.repository.ITeamDao;
import com.danielcsant.gitlab.repository.ProjectDaoMySqlImpl;
import com.danielcsant.gitlab.repository.TeamDaoMySqlImpl;
import com.danielcsant.gitlab.service.*;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.models.Issue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class App {

    private final static Logger LOGGER = LoggerFactory.getLogger("com.danielcsant.gitlab.App");

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
        String columnNames[] = {"Open","Week +1","Week +2","To Do","Doing","Desplegado en Test","Despliegue Pendiente","Desplegado","Closed"};
        String sheetId = prop.getProperty("sheetId");

        gitlabService = new CFDMetricsService(hostUrl, personalAccessToken);
        expeditesMetricsService = new ExpeditesMetricsService(hostUrl, personalAccessToken);
        newTasksMetricsService = new NewTasksMetricsService(hostUrl, personalAccessToken);
        testCoverageMetricsService = new TestCoverageMetricsService(hostUrl, personalAccessToken);

//        generateProjectMetrics(prop, columnNames);
//        generateTeamMetrics(prop, columnNames);
        generateExpediteMetrics(prop);
    }

    private static void generateExpediteMetrics(Properties prop) throws GitLabApiException {
        String [] teams = prop.getProperty("teams").split(",");
        String [] teamLabels = prop.getProperty("teamLabels").split(",");
        expeditesMetricsService.persistExpedites(teams, teamLabels);
    }

    private static void generateTeamMetrics(Properties prop, String[] columnNames) throws Exception {
        ITeamDao iTeamDao = new TeamDaoMySqlImpl();
        ArrayList metrics;
        metrics = new ArrayList();
        String [] teams = prop.getProperty("teams").split(",");
        ArrayList teamMetrics = new ArrayList();
        String teamName;
        for (int i = 0; i < teams.length; i++) {
            String [] teamData = teams[i].split(":");
            teamName = teamData[0];
            int closedAtStart = Integer.parseInt(teamData[1]);

            LOGGER.info("Retrieving data from team: " + teamName);
            HashMap<String, List<Issue>> columns = gitlabService.getColumnsMapForTeam(teamName, columnNames);
            TeamMetric newProjectMetric = generateTodayTeamMetrics(teamName, columnNames, columns, closedAtStart);
            LOGGER.info("Retrieved");

            metrics.add(newProjectMetric);
        }

        LOGGER.info("Inserting metrics for " + metrics.size() + " teams");
        boolean inserted2 = iTeamDao.insert("team", metrics);
        if (inserted2){
            LOGGER.info("Inserted");
        } else {
            LOGGER.warn("No metrics inserted");
        }
    }

    private static TeamMetric generateTodayTeamMetrics(String teamName, String[] columnNames, HashMap<String, List<Issue>> columns, int closedAtStart) throws Exception {
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

        List<Issue> bugsCreatedYesterday = expeditesMetricsService.getExpeditesCreatedYesterdayForTeam(teamName, columns);
        columnMap.put("Expedites", bugsCreatedYesterday.size());

        TeamMetric newTeamMetric = new TeamMetric(
                sqlDate,
                columnMap.get("Open"),
                columnMap.get("Week +1"),
                columnMap.get("Week +2"),
                columnMap.get("To Do"),
                columnMap.get("Doing"),
                columnMap.get("Desplegado en Test"),
                columnMap.get("Despliegue Pendiente"),
                columnMap.get("Desplegado"),
                columnMap.get("Closed"),
                teamName
        );

        return newTeamMetric;
    }

    private static void generateProjectMetrics(Properties prop, String[] columnNames) throws Exception {
        ArrayList metrics = new ArrayList();
        String [] projects = prop.getProperty("projects").split(",");

        for (int i = 0; i < projects.length; i++) {
            String [] projectData = projects[i].split(":");
            projectName = projectData[0];
            int closedAtStart = Integer.parseInt(projectData[1]);

            LOGGER.info("Retrieving data from project: " + projectName);
            HashMap<String, List<Issue>> columns = gitlabService.getColumnsMap(projectName, columnNames);
            ProjectMetric newProjectMetric = generateTodayMetrics(columnNames, columns, closedAtStart);
            LOGGER.info("Retrieved");

            metrics.add(newProjectMetric);
        }

        LOGGER.info("Inserting metrics for " + metrics.size() + " projects");

        IProjectDao iProjectDao = new ProjectDaoMySqlImpl();
        boolean inserted = iProjectDao.insert("project", metrics);
        if (inserted){
            LOGGER.info("Inserted");
        } else {
            LOGGER.warn("No metrics inserted");
        }
    }

    private static ProjectMetric generateTodayMetrics(String[] columnNames, HashMap<String, List<Issue>> columns, int closedAtStart) throws Exception {
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

        List<Issue> bugsCreatedYesterday = expeditesMetricsService.getExpeditesCreatedYesterday(projectName, columns);
        columnMap.put("Expedites", bugsCreatedYesterday.size());

        List<Issue> tasksCreatedYesterday = newTasksMetricsService.getTasksCreatedYesterday(columns);
        columnMap.put("New tasks", tasksCreatedYesterday.size());

        TestCoverage testCoverage = testCoverageMetricsService.getTestCoverage(projectName);
        columnMap.put("Coverage", (int) Double.parseDouble(testCoverage.getCoverage()));

        ProjectMetric newProjectMetric = new ProjectMetric(
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

        return newProjectMetric;
    }

}
