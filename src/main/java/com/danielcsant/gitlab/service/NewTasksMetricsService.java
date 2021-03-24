package com.danielcsant.gitlab.service;

import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.models.Issue;

import java.util.*;

public class NewTasksMetricsService extends GitlabService{

    public NewTasksMetricsService(String hostUrl, String personalAccessToken) throws GitLabApiException {
        super(hostUrl, personalAccessToken);
    }


    public List<Issue> getTasksCreatedYesterday(HashMap<String, List<Issue>> columns) throws Exception {
        List<Issue> result = new ArrayList<>();
        for (List<Issue> valuesColumn : columns.values()) {
            for (Issue issue : valuesColumn) {
                if (wasCreatedYesterday(issue)){
                    result.add(issue);
                }
            }
        }

        return result;
    }

    private boolean wasCreatedYesterday(Issue issue) {
        String createdDateString = issue.getCreatedAt().toString();
        String createdDateDay = createdDateString.substring(0, 10);

        Date yesterdayDate = getYesterdayDate();
        String yesterdayDayString = yesterdayDate.toString();
        String yesterdayDayDay = yesterdayDayString.substring(0, 10);

        return createdDateDay.equals(yesterdayDayDay);
    }

}
