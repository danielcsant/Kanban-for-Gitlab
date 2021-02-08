package com.danielcsant.gitlab.service;

import org.junit.Test;

import java.io.InputStream;
import java.util.Properties;

import static org.junit.Assert.*;

public class TestCoverageMetricsServiceManualTest {

    @Test
    public void getTestCoverageLastWorkingDayTest() throws Exception {
        Properties prop = new Properties();
        InputStream is = ClassLoader.getSystemResourceAsStream("app.properties");
        prop.load(is);
        String personalAccessToken = prop.getProperty("personalAccessToken");
        String hostUrl = prop.getProperty("hostUrl");
        String [] projects = prop.getProperty("projects").split(",");

        TestCoverageMetricsService testCoverageMetricsService = new TestCoverageMetricsService(hostUrl, personalAccessToken);

        testCoverageMetricsService.getTestCoverageLastWorkingDay(projects[0].split(":")[0]);
    }

}