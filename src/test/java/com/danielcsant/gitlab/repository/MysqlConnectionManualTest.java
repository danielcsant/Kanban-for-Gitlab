package com.danielcsant.gitlab.repository;

import com.danielcsant.gitlab.model.Metric;
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
        IMetricDao iMetricDao = new MetricDaoMySqlImpl();
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Date myDate = formatter.parse("2020-12-1");
        java.sql.Date sqlDate = new java.sql.Date(myDate.getTime());
        Metric newMetric = new Metric(
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

        boolean result = iMetricDao.insert("project", newMetric);

        Assert.assertTrue(result);
    }

    @Test
    public void validateTableNameTest() throws Exception {
        IMetricDao iMetricDao = new MetricDaoMySqlImpl();
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Date myDate = formatter.parse("2020-12-1");
        java.sql.Date sqlDate = new java.sql.Date(myDate.getTime());
        Metric newMetric = new Metric(
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

        boolean result = iMetricDao.insert("project", newMetric);

        Assert.assertTrue(result);
    }

    @Test
    public void bulkInsertMetricTest() throws Exception {
        IMetricDao iMetricDao = new MetricDaoMySqlImpl();
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        List metricList = new ArrayList();
        for (int i = 0; i < 10; i++) {
            Date myDate = formatter.parse("2020-12-" + i);
            java.sql.Date sqlDate = new java.sql.Date(myDate.getTime());
            Metric newMetric = new Metric(
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
            metricList.add(newMetric);
        }


        boolean result = iMetricDao.insert("project", metricList);

        Assert.assertTrue(result);
    }

}