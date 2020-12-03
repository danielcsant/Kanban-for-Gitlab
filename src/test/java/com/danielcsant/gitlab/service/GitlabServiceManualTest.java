package com.danielcsant.gitlab.service;

import org.gitlab4j.api.GitLabApiException;
import org.junit.BeforeClass;

import java.io.*;
import java.util.Properties;

public class GitlabServiceManualTest {

    private static CFDMetricsService gitlabService;

    static String projectName;
    static String columnNames[];

    @BeforeClass
    public static void init() throws GitLabApiException, IOException {
        Properties prop = new Properties();

        InputStream is = ClassLoader.getSystemResourceAsStream("app.properties");
        prop.load(is);

        String personalAccessToken = prop.getProperty("personalAccessToken");
        String hostUrl = prop.getProperty("hostUrl");
        projectName = prop.getProperty("projectName");
        columnNames = prop.getProperty("columns").split(",");
        int closedAtStart = Integer.parseInt(prop.getProperty("closedAtStart", "0"));

        gitlabService = new CFDMetricsService(hostUrl, personalAccessToken);
    }

}