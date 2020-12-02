package com.danielcsant.gitlab.repository;

import com.danielcsant.gitlab.model.Metric;
import org.junit.Assert;
import org.junit.Test;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

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
                10
        );

        iMetricDao.insert("naturitas", newMetric);

        Assert.assertTrue(true);
    }

}