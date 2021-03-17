package com.danielcsant.gitlab.service;

import org.gitlab4j.api.GitLabApiException;
import org.junit.Assert;
import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.junit.Assert.*;

public class ExpeditesMetricsServiceTest {

    @Test
    public void getHoursBetween() throws GitLabApiException, ParseException {
        ExpeditesMetricsService expeditesMetricsService = new ExpeditesMetricsService("foo", "bar");
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        Date date1 = simpleDateFormat.parse("2021-03-10T14:44:21.354+01:00");
        Date date2 = simpleDateFormat.parse("2021-03-10T18:44:21.354+01:00");

        Double result = expeditesMetricsService.getHoursBetween(date1, date2);

        Assert.assertEquals(4.0D, result, 0.1D);
    }

    @Test
    public void getLastDate() throws GitLabApiException, ParseException {
        ExpeditesMetricsService expeditesMetricsService = new ExpeditesMetricsService("foo", "bar");
        String dateString1 = "2021-03-10T18:44:21.354+01:00";
        String dateString2 = "2020-03-10T18:44:21.354+01:00";

        String result = expeditesMetricsService.getLastDate(dateString1, dateString2);

        Assert.assertEquals(dateString1, result);
    }
}