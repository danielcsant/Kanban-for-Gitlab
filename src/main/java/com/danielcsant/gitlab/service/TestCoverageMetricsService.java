package com.danielcsant.gitlab.service;

import com.danielcsant.gitlab.model.TestCoverage;
import org.gitlab4j.api.Constants;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.models.Pipeline;
import org.gitlab4j.api.models.PipelineFilter;
import org.gitlab4j.api.models.PipelineStatus;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class TestCoverageMetricsService extends GitlabService{

    private final static Logger LOGGER = Logger.getLogger("com.danielcsant.gitlab.service.TestCoverageMetricsService");

    public TestCoverageMetricsService(String hostUrl, String personalAccessToken) throws GitLabApiException {
        super(hostUrl, personalAccessToken);
    }

    public List<TestCoverage> getTestCoverageHistory(String projectName) throws Exception {
        List<TestCoverage> result = new ArrayList<>();
        HashMap<String, TestCoverage> testCoverageHashMap = new HashMap<>();

        TestCoverage testCoverage = null;
        PipelineFilter pipelineFilter = new PipelineFilter();
        pipelineFilter.withRef("master");
        List<Pipeline> pipelineList = gitLabApi.getPipelineApi()
                .getPipelines(getProject(projectName), pipelineFilter);
        for (Pipeline pipeline : pipelineList) {
            if (pipeline.getStatus() == PipelineStatus.SUCCESS) {
                Date updatedAt = pipeline.getUpdatedAt();

                if (!testCoverageHashMap.containsKey(updatedAt)){
                    String coverage = gitLabApi.getPipelineApi()
                            .getPipeline(getProject(projectName), pipeline.getId())
                            .getCoverage();
                    if (coverage == null || coverage.equals("")){
                        coverage = "0";
                    }
                    testCoverage = new TestCoverage(updatedAt, coverage);
                    testCoverageHashMap.put(getFormattedDate(updatedAt), testCoverage);
                }
            }
        }

        result = testCoverageHashMap.values().stream().collect(Collectors.toList());
        Collections.sort(result);
        return result;
    }

    public TestCoverage getTestCoverageLastWorkingDay(String projectName) throws Exception {

        TestCoverage testCoverage = null;
        PipelineFilter pipelineFilter = new PipelineFilter();
        pipelineFilter.withRef("master");
        pipelineFilter.withOrderBy(Constants.PipelineOrderBy.UPDATED_AT);
        pipelineFilter.setSort(Constants.SortOrder.DESC);
        List<Pipeline> pipelineList = gitLabApi.getPipelineApi()
                .getPipelines(getProject(projectName), pipelineFilter);
        for (Pipeline pipeline : pipelineList) {
            if (pipeline.getStatus() == PipelineStatus.SUCCESS) {
                String coverage = gitLabApi.getPipelineApi()
                        .getPipeline(getProject(projectName), pipeline.getId())
                        .getCoverage();
                if (coverage == null || coverage.equals("")){
                    coverage = "0";
                }
                testCoverage = new TestCoverage(new Date(), coverage);
                break;
            }
        }

        if (testCoverage == null) {
            LOGGER.warning("Master branch in project " + projectName + " does not exist.");
            testCoverage = new TestCoverage(new Date(),"0");
        }

        return testCoverage;
    }

    private boolean wasUpdatedInLastLaborDay(Date pipelineDate) {
        String updatedDateString = pipelineDate.toString();
        String updatedDateDay = updatedDateString.substring(0, 10);

        Date previousWorkingDay = getPreviousWorkingDay();
        String previousWorkingDayString = previousWorkingDay.toString();
        String previousWorkingDayDay = previousWorkingDayString.substring(0, 10);

        return updatedDateDay.equals(previousWorkingDayDay);
    }

    private static String getFormattedDate(Date date) {
        DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        return formatter.format(date);
    }


}
