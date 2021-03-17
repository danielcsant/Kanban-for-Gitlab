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

    public void persistExpedites(String[] teams, String[] teamLabels) throws GitLabApiException {

        HashMap<String, String> teamCompatibleLabels = new HashMap();
        for (int i = 0; i < teamLabels.length; i++) {
            String [] teamLabelsData = teamLabels[i].split(":");
            for (int j = 1; j < teamLabelsData.length; j++) {
                teamCompatibleLabels.put(teamLabelsData[j], teamLabelsData[0]);
            }
        }

        ArrayList<String> teamNames = new ArrayList();
        for (int i = 0; i < teams.length; i++) {
            String [] teamData = teams[i].split(":");
            teamNames.add(teamData[0]);
            teamCompatibleLabels.put(teamData[0], teamData[0]);
        }

        ArrayList expedites = new ArrayList();
        for (Issue issue : getAllIssues()) {
            if (issue.getLabels() != null && issue.getLabels().size() > 0){
                if (issue.getLabels().contains(EXPEDITE_LABEL)) {
                    Set<String> intersection = teamCompatibleLabels.keySet().stream()
                            .distinct()
                            .filter(issue.getLabels()::contains)
                            .collect(Collectors.toSet());

                    Double resolutionHours = getResolutionHours(issue);

                    if (intersection.size() > 0) {
                        String teamName = teamCompatibleLabels.get(intersection.iterator().next());
                        java.sql.Date expediteDate = new java.sql.Date(issue.getCreatedAt().getTime());
                        String projectName = getProjectName(issue.getProjectId());
                        ExpediteMetric expediteMetric = new ExpediteMetric(
                                expediteDate,
                                projectName,
                                issue.getIid(),
                                teamName,
                                issue.getTitle(),
                                issue.getWebUrl(),
                                resolutionHours
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

    private Double getResolutionHours(Issue issue) {

        String desplieguePendienteDateString = null;
        String desplegadoDateString = null;
        Date resolutionDate = null;
        try {
            List<LabelEvent> labelEvents = gitLabApi.getResourceLabelEventsApi().getIssueLabelEvents(issue.getProjectId(),issue.getIid());
            for (LabelEvent labelEvent : labelEvents) {
                if (labelEvent.getAction().equals("add")) {
                    switch (labelEvent.getLabel().getName()) {
                        case "Despliegue pendiente":
                            if (desplieguePendienteDateString == null ){
                                desplieguePendienteDateString = labelEvent.getCreatedAt();
                            } else {
                                desplieguePendienteDateString = getLastDate(desplieguePendienteDateString, labelEvent.getCreatedAt());
                            }
                        case "Desplegado":
                            if (desplegadoDateString == null ){
                                desplegadoDateString = labelEvent.getCreatedAt();
                            } else {
                                desplegadoDateString = getLastDate(desplegadoDateString, labelEvent.getCreatedAt());
                            }
                    }
                }
            }

            if (desplieguePendienteDateString == null) {
                if (desplegadoDateString == null) {
                    if (issue.getClosedAt() != null) {
                        resolutionDate = issue.getClosedAt();
                    }
                } else {
                    resolutionDate = getDate(desplegadoDateString);
                }
            } else {
                resolutionDate = getDate(desplieguePendienteDateString);
            }

        } catch (GitLabApiException e) {
            LOGGER.error("Error retrieving labels for proyect id " + issue.getProjectId(), e);
        } catch (ParseException e) {
            LOGGER.error("Error formatting date", e);
        }

        Double resolutionHours = null;
        if (resolutionDate != null) {
            resolutionHours = getHoursBetween(issue.getCreatedAt(), resolutionDate);
        }

        return resolutionHours;
    }

    protected Double getHoursBetween(Date createdAt, Date resolutionDate) {
        long secs = (resolutionDate.getTime() - createdAt.getTime()) / 1000;
        return (double) secs / 3600;
    }

    protected String getLastDate(String dateString1, String dateString2) throws ParseException {
        String lastDate;
        Date date1 = getDate(dateString1);
        Date date2 = getDate(dateString2);

        if (date1.after(date2)) {
            lastDate = dateString1;
        } else {
            lastDate = dateString2;
        }

        return lastDate;
    }
}
