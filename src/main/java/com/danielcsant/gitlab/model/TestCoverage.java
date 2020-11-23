package com.danielcsant.gitlab.model;

import java.util.Date;

public class TestCoverage implements Comparable<TestCoverage>{

    private Date updatedAt;
    private String coverage;

    public TestCoverage(Date updatedAt, String coverage) {
        this.coverage = coverage;
        this.updatedAt = updatedAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public String getCoverage() {
        return coverage;
    }

    @Override
    public int compareTo(TestCoverage o) {
        return updatedAt.compareTo(o.getUpdatedAt());
    }
}
