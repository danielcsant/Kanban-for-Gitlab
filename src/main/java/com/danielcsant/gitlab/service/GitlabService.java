package com.danielcsant.gitlab.service;

import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.models.Issue;
import org.gitlab4j.api.models.Project;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public abstract class GitlabService {

    GitLabApi gitLabApi = null;
    List<Project> projects = null;

    public GitlabService(String hostUrl, String personalAccessToken) throws GitLabApiException {
        // Create a GitLabApi instance to communicate with your GitLab server
        gitLabApi = new GitLabApi(hostUrl, personalAccessToken);

        // Get the list of projects your account has access to
        getProjectList();
    }

    private void getProjectList() throws GitLabApiException {
        if (projects == null) {
            projects = gitLabApi.getProjectApi().getProjects();
        }
    }

    protected Project getProject(String projectName) throws Exception {
        Project project = null;
        for (Project projectIter : projects) {
            if (projectIter.getName().equalsIgnoreCase(projectName)){
                project = projectIter;
                break;
            }
        }

        if (project == null){
            throw new Exception("Project not found");
        }
        return project;
    }

    protected Date getDate(String date) throws ParseException {
        return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX").parse(date);
    }

    protected Date getPreviousWorkingDay() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());

        int dayOfWeek;
        do {
            cal.add(Calendar.DAY_OF_MONTH, -1);
            dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        } while (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY);

        return cal.getTime();
    }
}
