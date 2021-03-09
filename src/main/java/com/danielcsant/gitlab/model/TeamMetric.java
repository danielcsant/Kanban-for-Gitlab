package com.danielcsant.gitlab.model;


import java.sql.Date;

public class TeamMetric {

    private Date metricDate;

    // CFD
    private int open;
    private int week1;
    private int week2;
    private int toDo;
    private int doing;
    private int desplegadoEnTest;
    private int desplieguePendiente;
    private int desplegado;
    private int closed;


    private String project;


    public TeamMetric(Date metricDate, int open, int week1, int week2, int toDo, int doing, int desplegadoEnTest, int desplieguePendiente, int desplegado, int closed, String project) {
        this.metricDate = metricDate;
        this.open = open;
        this.week1 = week1;
        this.week2 = week2;
        this.toDo = toDo;
        this.doing = doing;
        this.desplegadoEnTest = desplegadoEnTest;
        this.desplieguePendiente = desplieguePendiente;
        this.desplegado = desplegado;
        this.closed = closed;
        this.project = project;
    }

    public Date getMetricDate() {
        return metricDate;
    }

    public void setMetricDate(Date metricDate) {
        this.metricDate = metricDate;
    }

    public int getOpen() {
        return open;
    }

    public void setOpen(int open) {
        this.open = open;
    }

    public int getToDo() {
        return toDo;
    }

    public void setToDo(int toDo) {
        this.toDo = toDo;
    }

    public int getDoing() {
        return doing;
    }

    public void setDoing(int doing) {
        this.doing = doing;
    }

    public int getDesplegadoEnTest() {
        return desplegadoEnTest;
    }

    public void setDesplegadoEnTest(int desplegadoEnTest) {
        this.desplegadoEnTest = desplegadoEnTest;
    }

    public int getDesplieguePendiente() {
        return desplieguePendiente;
    }

    public void setDesplieguePendiente(int desplieguePendiente) {
        this.desplieguePendiente = desplieguePendiente;
    }

    public int getDesplegado() {
        return desplegado;
    }

    public void setDesplegado(int desplegado) {
        this.desplegado = desplegado;
    }

    public int getClosed() {
        return closed;
    }

    public void setClosed(int closed) {
        this.closed = closed;
    }

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public int getWeek1() {
        return week1;
    }

    public void setWeek1(int week1) {
        this.week1 = week1;
    }

    public int getWeek2() {
        return week2;
    }

    public void setWeek2(int week2) {
        this.week2 = week2;
    }
}
