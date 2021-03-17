package com.danielcsant.gitlab.model;


import java.sql.Date;

public class ExpediteMetric {

    private Date metricDate;
    private String project;
    private int iid;
    private String team;
    private String title;
    private String url;
    Double resolutionHours;

    public ExpediteMetric(Date metricDate, String project, int iid, String team, String title, String url, Double resolutionHours) {
        this.metricDate = metricDate;
        this.project = project;
        this.iid = iid;
        this.team = team;
        this.title = title;
        this.url = url;
        this.resolutionHours = resolutionHours;
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

    public int getIid() {
        return iid;
    }

    public void setIid(int iid) {
        this.iid = iid;
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

    public Double getResolutionHours() {
        return resolutionHours;
    }

    public void setResolutionHours(Double resolutionHours) {
        this.resolutionHours = resolutionHours;
    }
}
