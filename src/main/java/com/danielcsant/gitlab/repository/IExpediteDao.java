package com.danielcsant.gitlab.repository;

import com.danielcsant.gitlab.model.ExpediteMetric;
import com.danielcsant.gitlab.model.ProjectMetric;

import java.util.List;

public interface IExpediteDao {

    public boolean upsert(String tableName, List<ExpediteMetric> expediteMetrics);

}
