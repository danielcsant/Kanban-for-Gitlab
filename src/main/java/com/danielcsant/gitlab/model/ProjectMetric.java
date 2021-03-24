package com.danielcsant.gitlab.model;


import java.sql.Date;

public class ProjectMetric {

    private Date metricDate;

    // CFD
    private int open;
    private int toDo;
    private int doing;
    private int desplegadoEnTest;
    private int desplieguePendiente;
    private int desplegado;
    private int closed;

    // Bugs
    private int newBugs;

    // New Tasks
    private int newTasks;

    private String project;


    public ProjectMetric(Date metricDate, int open, int toDo, int doing, int desplegadoEnTest, int desplieguePendiente, int desplegado, int closed, int newBugs, int newTasks, String project) {
        this.metricDate = metricDate;
        this.open = open;
        this.toDo = toDo;
        this.doing = doing;
        this.desplegadoEnTest = desplegadoEnTest;
        this.desplieguePendiente = desplieguePendiente;
        this.desplegado = desplegado;
        this.closed = closed;
        this.newBugs = newBugs;
        this.newTasks = newTasks;
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

    public int getNewBugs() {
        return newBugs;
    }

    public void setNewBugs(int newBugs) {
        this.newBugs = newBugs;
    }

    public int getNewTasks() {
        return newTasks;
    }

    public void setNewTasks(int newTasks) {
        this.newTasks = newTasks;
    }

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }
}
