package com.danielcsant.gitlab.repository;

import com.danielcsant.gitlab.model.ExpediteMetric;
import com.danielcsant.gitlab.model.TestCoverage;

import java.util.List;

public interface ICoverageDao {

    public boolean insert(String tableName, List<TestCoverage> testCoverageList);

}
