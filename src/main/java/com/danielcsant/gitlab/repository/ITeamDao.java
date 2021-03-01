package com.danielcsant.gitlab.repository;

import com.danielcsant.gitlab.model.ProjectMetric;
import com.danielcsant.gitlab.model.TeamMetric;

import java.util.List;

public interface ITeamDao {

    public boolean insert(String tableName, List<TeamMetric> projectMetrics);
    public boolean insert(String tableName, TeamMetric projectMetric);

}
