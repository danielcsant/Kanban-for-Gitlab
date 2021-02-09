package com.danielcsant.gitlab.service;

import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.models.Issue;
import org.gitlab4j.api.models.LabelEvent;

import java.text.ParseException;
import java.util.*;

public class ExpeditesMetricsService extends GitlabService{

    final String EXPEDITE_LABEL = "Expedite";

    public ExpeditesMetricsService(String hostUrl, String personalAccessToken) throws GitLabApiException {
        super(hostUrl, personalAccessToken);
    }

    public List<Issue> getExpeditesCreatedYesterday(String pathWithNamespace, HashMap<String, List<Issue>> columns) throws Exception {
        List<Issue> result = new ArrayList<>();
        for (List<Issue> valuesColumn : columns.values()) {
            for (Issue issue : valuesColumn) {
                if (isExpedite(issue) && wasCreatedYesterday(pathWithNamespace, issue)){
                    result.add(issue);
                }
            }
        }

        return result;
    }

    private boolean wasCreatedYesterday(String pathWithNamespace, Issue issue) throws GitLabApiException, ParseException {
        List<LabelEvent> labelEvents = gitLabApi
                .getResourceLabelEventsApi()
                .getIssueLabelEvents(pathWithNamespace, issue.getIid());

        Date createdAsExpedite = null;
        for (LabelEvent labelEvent : labelEvents) {
            if (labelEvent.getLabel() != null &&
                    labelEvent.getLabel().getName().equals(EXPEDITE_LABEL) &&
                    labelEvent.getAction().equals("add")){
                createdAsExpedite = getDate(labelEvent.getCreatedAt());
                break;
            }
        }

        return createdAsExpedite != null && areSameDay(createdAsExpedite, getYesterdayDate());
    }

    private boolean areSameDay(Date asExpedite, Date previousWorkingDay) {
        String asExpediteString = asExpedite.toString();
        String asExpediteStringDay = asExpediteString.substring(0, 10);

        String previousWorkingDayString = previousWorkingDay.toString();
        String previousWorkingDayDay = previousWorkingDayString.substring(0, 10);

        return asExpediteStringDay.equals(previousWorkingDayDay);
    }

    private boolean isExpedite(Issue issue) {
        return issue.getLabels() != null && issue.getLabels().contains(EXPEDITE_LABEL);
    }


}
