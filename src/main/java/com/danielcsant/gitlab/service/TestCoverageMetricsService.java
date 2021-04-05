package com.danielcsant.gitlab.service;

import com.danielcsant.gitlab.model.TestCoverage;
import com.danielcsant.gitlab.repository.CoverageDaoMySqlImpl;
import com.danielcsant.gitlab.repository.TeamDaoMySqlImpl;
import org.gitlab4j.api.Constants;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.models.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class TestCoverageMetricsService extends GitlabService{

    private final static Logger LOGGER = LoggerFactory.getLogger(TestCoverageMetricsService.class);

    public TestCoverageMetricsService(String hostUrl, String personalAccessToken) throws GitLabApiException {
        super(hostUrl, personalAccessToken);
    }

    protected TestCoverage getTestCoverage(String pathWithNamespace) throws Exception {
        TestCoverage testCoverage = null;

        try {
            PipelineFilter pipelineFilter = new PipelineFilter();
            pipelineFilter.withRef("master");
            pipelineFilter.withOrderBy(Constants.PipelineOrderBy.UPDATED_AT);
            pipelineFilter.setSort(Constants.SortOrder.DESC);
            List<Pipeline> pipelineList = gitLabApi.getPipelineApi()
                    .getPipelines(pathWithNamespace, pipelineFilter);

            java.sql.Date today = new java.sql.Date(new Date().getTime());

            for (Pipeline pipeline : pipelineList) {
                if (pipeline.getStatus() == PipelineStatus.SUCCESS) {
                    String coverage = gitLabApi.getPipelineApi()
                            .getPipeline(pathWithNamespace, pipeline.getId())
                            .getCoverage();
                    if (coverage == null || coverage.equals("")){
                        coverage = "0";
                    }
                    testCoverage = new TestCoverage(today, pathWithNamespace, coverage);
                    break;
                }
            }

            if (pipelineList.isEmpty()){
                LOGGER.warn("No pipelines configured in project " + pathWithNamespace);
                testCoverage = new TestCoverage(today, pathWithNamespace, "0");
            } else if (testCoverage == null) {
                LOGGER.warn("Master branch in project " + pathWithNamespace + " does not exist.");
                testCoverage = new TestCoverage(today, pathWithNamespace, "0");
            }

        } catch (GitLabApiException gitLabApiException) {
            LOGGER.error("Error getting coverage from project:" + pathWithNamespace + ".");
        }

        return testCoverage;
    }

    public void measureCoverage(String groupName) throws Exception {
        List<TestCoverage> testCoverageList = new ArrayList<>();

        List<Project> projectList = gitLabApi.getGroupApi().getProjects(groupName);

        for (Project project : projectList) {
            if (!project.getArchived()){
                TestCoverage testCoverage = getTestCoverage(project.getPathWithNamespace());
                if (testCoverage != null) {
                    testCoverageList.add(testCoverage);
                }
            } else {
                LOGGER.info("Project " + project.getPathWithNamespace() + " is archived. Skipping coverage.");
            }
        }

        if (!testCoverageList.isEmpty()){
            CoverageDaoMySqlImpl coverageDaoMySql = new CoverageDaoMySqlImpl();
            coverageDaoMySql.insert("coverage", testCoverageList);
        }
    }
}
