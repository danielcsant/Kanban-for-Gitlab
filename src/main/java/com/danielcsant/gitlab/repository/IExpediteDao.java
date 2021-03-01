package com.danielcsant.gitlab.repository;

import com.danielcsant.gitlab.model.ExpediteMetric;
import com.danielcsant.gitlab.model.ProjectMetric;

import java.util.List;

public interface IExpediteDao {

    public boolean insert(String tableName, List<ExpediteMetric> expediteMetrics);
    public boolean insert(String tableName, ExpediteMetric expediteMetric);

}
