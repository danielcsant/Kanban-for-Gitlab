package com.danielcsant.gitlab.repository;

import com.danielcsant.gitlab.model.ProjectMetric;

import java.util.List;

public interface IProjectDao {

    public boolean insert(String tableName, List<ProjectMetric> projectMetrics);
    public boolean insert(String tableName, ProjectMetric projectMetric);

}
