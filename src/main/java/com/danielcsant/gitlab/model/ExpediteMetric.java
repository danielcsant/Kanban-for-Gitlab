package com.danielcsant.gitlab.model;


import java.sql.Date;

public class ExpediteMetric {

    private Date metricDate;
    private String project;
    private String team;
    private String title;
    private String url;

    public ExpediteMetric(Date metricDate, String project, String team, String title, String url) {
        this.metricDate = metricDate;
        this.project = project;
        this.team = team;
        this.title = title;
        this.url = url;
    }

    public Date getMetricDate() {
        return metricDate;
    }

    public void setMetricDate(Date metricDate) {
        this.metricDate = metricDate;
    }

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public String getTeam() {
        return team;
    }

    public void setTeam(String team) {
        this.team = team;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
