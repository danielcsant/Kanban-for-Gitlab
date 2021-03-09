package com.danielcsant.gitlab.service;

import com.danielcsant.gitlab.model.ExpediteMetric;
import com.danielcsant.gitlab.repository.ExpediteDaoMySqlImpl;
import com.danielcsant.gitlab.repository.IExpediteDao;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.models.Issue;
import org.gitlab4j.api.models.IssueFilter;
import org.gitlab4j.api.models.LabelEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

public class ExpeditesMetricsService extends GitlabService{

    private final static Logger LOGGER = LoggerFactory.getLogger(ExpeditesMetricsService.class);

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

    public void persistExpedites(String[] teams) throws GitLabApiException {


        ArrayList<String> teamNames = new ArrayList();
        for (int i = 0; i < teams.length; i++) {
            String [] teamData = teams[i].split(":");
            teamNames.add(teamData[0]);
        }

        ArrayList expedites = new ArrayList();
        for (Issue issue : getAllIssues()) {
            if (issue.getLabels() != null && issue.getLabels().size() > 0){
                if (issue.getLabels().contains(EXPEDITE_LABEL)) {
                    Set<String> intersection = teamNames.stream()
                            .distinct()
                            .filter(issue.getLabels()::contains)
                            .collect(Collectors.toSet());

                    if (intersection.size() > 0) {
                        java.sql.Date expediteDate = new java.sql.Date(issue.getCreatedAt().getTime());
                        String projectName = getProjectName(issue.getProjectId());
                        ExpediteMetric expediteMetric = new ExpediteMetric(
                                expediteDate,
                                projectName,
                                issue.getIid(),
                                intersection.iterator().next(),
                                issue.getTitle(),
                                issue.getWebUrl()
                        );

                        expedites.add(expediteMetric);
                    }
                }
            }
        }

        IExpediteDao expediteDao = new ExpediteDaoMySqlImpl();
        LOGGER.info("Inserting expedites");
        boolean inserted = expediteDao.upsert("expedite", expedites);
        if (inserted){
            LOGGER.info("Inserted");
        } else {
            LOGGER.warn("No metrics inserted");
        }
    }
}
