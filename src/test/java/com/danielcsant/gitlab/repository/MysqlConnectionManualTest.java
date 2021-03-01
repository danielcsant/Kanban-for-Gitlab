package com.danielcsant.gitlab.repository;

import com.danielcsant.gitlab.model.ProjectMetric;
import org.junit.Assert;
import org.junit.Test;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MysqlConnectionManualTest {

    @Test
    public void insertMetricTest() throws Exception {
        IProjectDao iProjectDao = new ProjectDaoMySqlImpl();
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Date myDate = formatter.parse("2020-12-1");
        java.sql.Date sqlDate = new java.sql.Date(myDate.getTime());
        ProjectMetric newProjectMetric = new ProjectMetric(
                sqlDate,
                1,
                2,
                3,
                4,
                5,
                6,
                7,
                8,
                9,
                10,
                "project foo"
        );

        boolean result = iProjectDao.insert("project", newProjectMetric);

        Assert.assertTrue(result);
    }

    @Test
    public void validateTableNameTest() throws Exception {
        IProjectDao iProjectDao = new ProjectDaoMySqlImpl();
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Date myDate = formatter.parse("2020-12-1");
        java.sql.Date sqlDate = new java.sql.Date(myDate.getTime());
        ProjectMetric newProjectMetric = new ProjectMetric(
                sqlDate,
                1,
                2,
                3,
                4,
                5,
                6,
                7,
                8,
                9,
                10,
                "project foo"
        );

        boolean result = iProjectDao.insert("project", newProjectMetric);

        Assert.assertTrue(result);
    }

    @Test
    public void bulkInsertMetricTest() throws Exception {
        IProjectDao iProjectDao = new ProjectDaoMySqlImpl();
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        List metricList = new ArrayList();
        for (int i = 0; i < 10; i++) {
            Date myDate = formatter.parse("2020-12-" + i);
            java.sql.Date sqlDate = new java.sql.Date(myDate.getTime());
            ProjectMetric newProjectMetric = new ProjectMetric(
                    sqlDate,
                    1,
                    2,
                    3,
                    4,
                    5,
                    6,
                    7,
                    8,
                    9,
                    10,
                    "project foo"
            );
            metricList.add(newProjectMetric);
        }


        boolean result = iProjectDao.insert("project", metricList);

        Assert.assertTrue(result);
    }

}