package com.danielcsant.gitlab.service;

import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.models.Issue;
import org.gitlab4j.api.models.LabelEvent;
import org.gitlab4j.api.models.Project;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class NewTasksMetricsService extends GitlabService{

    public NewTasksMetricsService(String hostUrl, String personalAccessToken, int closedAtStart) throws GitLabApiException {
        super(hostUrl, personalAccessToken, closedAtStart);
    }


    public List<Issue> getTasksCreatedYesterday(String projectName, HashMap<String, List<Issue>> columns) throws Exception {
        Project project = getProject(projectName);
        List<Issue> result = new ArrayList<>();
        for (List<Issue> valuesColumn : columns.values()) {
            for (Issue issue : valuesColumn) {
                if (wasCreatedInLastLaborDay(issue)){
                    result.add(issue);
                }
            }
        }

        return result;
    }

    private boolean wasCreatedInLastLaborDay(Issue issue) {
        String createdDateString = issue.getCreatedAt().toString();
        String createdDateDay = createdDateString.substring(0, 10);

        Date previousWorkingDay = getPreviousWorkingDay();
        String previousWorkingDayString = previousWorkingDay.toString();
        String previousWorkingDayDay = previousWorkingDayString.substring(0, 10);

        return createdDateDay.equals(previousWorkingDayDay);
    }
}
