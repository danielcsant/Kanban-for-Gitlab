package com.danielcsant.gitlab.service;

import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.models.Issue;
import org.gitlab4j.api.models.IssueFilter;
import org.gitlab4j.api.models.LabelEvent;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class ExpeditesMetricsService extends GitlabService{

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


    public List<Issue> getExpeditesCreatedYesterdayForTeam(String teamName, HashMap<String, List<Issue>> columns) throws GitLabApiException {

        List result = new ArrayList();

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
        cal.set(Calendar.HOUR, 0);
        cal.set(Calendar.MINUTE, 1);

        IssueFilter issueFilterCreatedYesterday = new IssueFilter();
        issueFilterCreatedYesterday.setCreatedAfter(cal.getTime());

        // Get a list of issues for the specified project ID
        List<Issue> issues = gitLabApi.getIssuesApi().getIssues(issueFilterCreatedYesterday);

        for (Issue issue : issues) {
            if (issue.getLabels() != null && issue.getLabels().size() > 0){
                if (issue.getLabels().contains(EXPEDITE_LABEL) && issue.getLabels().contains(teamName)) {
                    result.add(issue);
                }
            }
        }

        return result;
    }
}
