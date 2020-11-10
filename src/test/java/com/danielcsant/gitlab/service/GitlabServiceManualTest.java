package com.danielcsant.gitlab.service;

import com.danielcsant.gitlab.model.ColumnStatus;
import com.danielcsant.gitlab.model.IssueColumnStatuses;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.models.Issue;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
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

        gitlabService = new CFDMetricsService(hostUrl, personalAccessToken, closedAtStart);
    }

    @Test
    public void getIssuesStatuses() throws Exception {
        Issue issue = gitlabService.getIssue(projectName, 1294);
        HashMap<String, List<Issue>> columns = new HashMap<>();
        columns.put("Closed", Arrays.asList(issue));

        List<IssueColumnStatuses> result = gitlabService.getIssuesStatuses(projectName, columns, columnNames);

        Assert.assertEquals(1, result.size());
        ColumnStatus doingColumnStatus = result.get(0).getColumnStatusHashMap().get("Doing");
        Assert.assertNotNull(doingColumnStatus.getAddedDate());
        Assert.assertNotNull(doingColumnStatus.getRemovedDate());
    }
}