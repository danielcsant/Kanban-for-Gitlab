package com.danielcsant.gitlab.model;

import java.sql.Date;

public class TestCoverage implements Comparable<TestCoverage>{

    private Date metricDate;
    private String project;
    private String coverage;

    public TestCoverage(Date metricDate, String project, String coverage) {
        this.metricDate = metricDate;
        this.project = project;
        this.coverage = coverage;
    }

    public Date getMetricDate() {
        return metricDate;
    }

    public void setMetricDate(Date metricDate) {
        this.metricDate = metricDate;
    }

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public void setCoverage(String coverage) {
        this.coverage = coverage;
    }

    public String getCoverage() {
        return coverage;
    }

    @Override
    public int compareTo(TestCoverage o) {
        return metricDate.compareTo(o.getMetricDate());
    }
}
