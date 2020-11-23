package com.danielcsant.gitlab;

import com.danielcsant.gitlab.model.TestCoverage;
import com.danielcsant.gitlab.service.SheetsService;
import com.danielcsant.gitlab.service.TestCoverageMetricsService;

import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class InitializeCoverageMetrics {

    private static SheetsService sheetsService;
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

        sheetsService = new SheetsService(sheetId);
        testCoverageMetricsService = new TestCoverageMetricsService(hostUrl, personalAccessToken);

        generateCoverageMetrics();
    }

    private static void generateCoverageMetrics() throws Exception {

        List<TestCoverage> testCoverageList = testCoverageMetricsService.getTestCoverageHistory(projectName);
        List<List<Object>> newRows = new ArrayList<>();

        for (TestCoverage testCoverage : testCoverageList) {
            ArrayList newTaskRow = new ArrayList();
            newTaskRow.add(getFormattedDate(testCoverage.getUpdatedAt()));
            newTaskRow.add(Double.parseDouble(testCoverage.getCoverage()));
            newRows.add(newTaskRow);
        }

        sheetsService.persistNewRow("Coverage", newRows);
    }

    private static String getFormattedDate(Date date) {
        DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        return formatter.format(date);
    }

}
