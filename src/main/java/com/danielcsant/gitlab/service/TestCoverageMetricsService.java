package com.danielcsant.gitlab.service;

import com.danielcsant.gitlab.model.TestCoverage;
import org.gitlab4j.api.Constants;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.models.Pipeline;
import org.gitlab4j.api.models.PipelineFilter;
import org.gitlab4j.api.models.PipelineStatus;
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

    public List<TestCoverage> getTestCoverageHistory(String pathWithNamespace) throws Exception {
        List<TestCoverage> result = new ArrayList<>();
        HashMap<String, TestCoverage> testCoverageHashMap = new HashMap<>();

        TestCoverage testCoverage = null;
        PipelineFilter pipelineFilter = new PipelineFilter();
        pipelineFilter.withRef("master");
        List<Pipeline> pipelineList = gitLabApi.getPipelineApi()
                .getPipelines(pathWithNamespace, pipelineFilter);
        for (Pipeline pipeline : pipelineList) {
            if (pipeline.getStatus() == PipelineStatus.SUCCESS) {
                Date updatedAt = pipeline.getUpdatedAt();

                if (!testCoverageHashMap.containsKey(updatedAt)){
                    String coverage = gitLabApi.getPipelineApi()
                            .getPipeline(pathWithNamespace, pipeline.getId())
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

    public TestCoverage getTestCoverageLastWorkingDay(String pathWithNamespace) throws Exception {

        TestCoverage testCoverage = null;
        PipelineFilter pipelineFilter = new PipelineFilter();
        pipelineFilter.withRef("master");
        pipelineFilter.withOrderBy(Constants.PipelineOrderBy.UPDATED_AT);
        pipelineFilter.setSort(Constants.SortOrder.DESC);
        List<Pipeline> pipelineList = gitLabApi.getPipelineApi()
                .getPipelines(pathWithNamespace, pipelineFilter);
        for (Pipeline pipeline : pipelineList) {
            if (pipeline.getStatus() == PipelineStatus.SUCCESS) {
                String coverage = gitLabApi.getPipelineApi()
                        .getPipeline(pathWithNamespace, pipeline.getId())
                        .getCoverage();
                if (coverage == null || coverage.equals("")){
                    coverage = "0";
                }
                testCoverage = new TestCoverage(new Date(), coverage);
                break;
            }
        }

        if (pipelineList.isEmpty()){
            LOGGER.warn("No pipelines configured in project " + pathWithNamespace);
            testCoverage = new TestCoverage(new Date(),"0");
        }else if (testCoverage == null) {
            LOGGER.warn("Master branch in project " + pathWithNamespace + " does not exist.");
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
