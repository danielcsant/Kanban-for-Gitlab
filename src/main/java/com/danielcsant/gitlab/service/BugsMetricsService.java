package com.danielcsant.gitlab.service;

import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.models.Issue;
import org.gitlab4j.api.models.LabelEvent;
import org.gitlab4j.api.models.Project;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class BugsMetricsService extends GitlabService{

    final String BUG_LABEL = "1- Critico";

    public BugsMetricsService(String hostUrl, String personalAccessToken, int closedAtStart) throws GitLabApiException {
        super(hostUrl, personalAccessToken, closedAtStart);
    }

    public List<Issue> getBugsCreatedYesterday(String projectName, HashMap<String, List<Issue>> columns) throws Exception {
        Project project = getProject(projectName);
        List<Issue> result = new ArrayList<>();
        for (List<Issue> valuesColumn : columns.values()) {
            for (Issue issue : valuesColumn) {
                if (isBug(issue) && wasCreatedInLastLaborDay(project, issue)){
                    result.add(issue);
                }
            }
        }

        return result;
    }

    private boolean wasCreatedInLastLaborDay(Project project, Issue issue) throws GitLabApiException, ParseException {
        List<LabelEvent> labelEvents = gitLabApi
                .getResourceLabelEventsApi()
                .getIssueLabelEvents(project.getId(), issue.getIid());

        Date createdAsBug = null;
        for (LabelEvent labelEvent : labelEvents) {
            if (labelEvent.getLabel() != null &&
                    labelEvent.getLabel().getName().equals(BUG_LABEL) &&
                    labelEvent.getAction().equals("add")){
                createdAsBug = getDate(labelEvent.getCreatedAt());
                break;
            }
        }

        return createdAsBug != null && areSameDay(createdAsBug, getPreviousWorkingDay());
    }

    private boolean areSameDay(Date asBug, Date previousWorkingDay) {
        String asBugString = asBug.toString();
        String asBugStringDay = asBugString.substring(0, 10);

        String previousWorkingDayString = previousWorkingDay.toString();
        String previousWorkingDayDay = previousWorkingDayString.substring(0, 10);

        return asBugStringDay.equals(previousWorkingDayDay);
    }

    private boolean isBug(Issue issue) {
        return issue.getLabels() != null && issue.getLabels().contains(BUG_LABEL);
    }


}
