package com.danielcsant.gitlab.service;

import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.models.Issue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CFDMetricsService extends GitlabService {

    private final static Logger LOGGER = LoggerFactory.getLogger(CFDMetricsService.class);

    public CFDMetricsService(String hostUrl, String personalAccessToken) throws GitLabApiException {
        super(hostUrl, personalAccessToken);
    }

    public HashMap<String, List<Issue>> getColumnsMap(String pathWithNamespace, String[] columnNames) throws Exception {

        HashMap<String, List<Issue>> columns = new HashMap<>();
        for (int i = 0; i < columnNames.length; i++) {
            String column = columnNames[i];
            columns.put(column, new ArrayList<>());
        }

        List<Issue> openedIssues = new ArrayList<>();
        List<Issue> reopenedIssues = new ArrayList<>();
        List<Issue> closedIssues = new ArrayList<>();
        // Get a list of issues for the specified project ID
        List<Issue> issues = gitLabApi.getIssuesApi().getIssues(pathWithNamespace);
        for (Issue issue : issues) {
            switch (issue.getState()) {
                case OPENED:
                    try {
                        boolean hasColumnLabel = false;
                        if (issue.getLabels() != null && issue.getLabels().size() > 0){
                            for (String label : issue.getLabels()) {
                                if (columns.containsKey(label)){
                                    hasColumnLabel = true;
                                    break;
                                }
                            }
                        }
                        if (!hasColumnLabel){
                            openedIssues.add(issue);
                        }
                    } catch (Exception e){
                        if (issue.getLabels() != null && issue.getLabels().size() > 0){
                            System.err.println(issue.getLabels());
                        }
                        throw e;
                    }
                    break;
                case CLOSED:
                    closedIssues.add(issue);
                    break;
                case REOPENED:
                    reopenedIssues.add(issue);
                    break;
            }

            switch (issue.getState()) {
                case OPENED:
                case REOPENED:
                    try {
                        if (issue.getLabels() != null && issue.getLabels().size() > 0){
                            for (String label : issue.getLabels()) {
                                if (columns.containsKey(label)){
                                    columns.get(label).add(issue);
                                }
                            }
                        }
                    } catch (Exception e){
                        if (issue.getLabels() != null && issue.getLabels().size() > 0){
                            System.err.println(issue.getLabels());
                        }
                        throw e;
                    }

                    break;
            }

        }

        columns.put("Open", openedIssues);
        columns.put("Closed", closedIssues);
        columns.put("Reopened", reopenedIssues);

        for (String s : columnNames) {
            LOGGER.debug("Issues in column " + s + ": " + columns.get(s).size());
        }

        return columns;
    }

}
