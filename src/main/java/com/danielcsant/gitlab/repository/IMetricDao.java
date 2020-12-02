package com.danielcsant.gitlab.repository;

import com.danielcsant.gitlab.model.Metric;

import java.util.List;

public interface IMetricDao {

    public boolean insert(String tableName, List<Metric> metrics);
    public boolean insert(String tableName, Metric metric);

}
