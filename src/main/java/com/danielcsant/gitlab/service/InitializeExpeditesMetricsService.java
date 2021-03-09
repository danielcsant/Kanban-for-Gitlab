package com.danielcsant.gitlab.service;

import com.danielcsant.gitlab.model.ExpediteMetric;
import com.danielcsant.gitlab.repository.ExpediteDaoMySqlImpl;
import com.danielcsant.gitlab.repository.IExpediteDao;
import org.gitlab4j.api.Constants;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.models.Issue;
import org.gitlab4j.api.models.IssueFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class InitializeExpeditesMetricsService extends GitlabService{

    private final static Logger LOGGER = LoggerFactory.getLogger(InitializeExpeditesMetricsService.class);

    public InitializeExpeditesMetricsService(String hostUrl, String personalAccessToken) throws GitLabApiException {
        super(hostUrl, personalAccessToken);
    }

    public void init(String[] teams) throws GitLabApiException {
        IssueFilter issueFilterOpen = new IssueFilter();
        issueFilterOpen.setScope(Constants.IssueScope.ALL);
        List<Issue> allIssues = gitLabApi.getIssuesApi().getIssues(issueFilterOpen);

        ArrayList<String> teamNames = new ArrayList();
        for (int i = 0; i < teams.length; i++) {
            String [] teamData = teams[i].split(":");
            teamNames.add(teamData[0]);
        }

        ArrayList expedites = new ArrayList();
        for (Issue issue : allIssues) {
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
                                issue.getIid(), intersection.iterator().next(),
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
