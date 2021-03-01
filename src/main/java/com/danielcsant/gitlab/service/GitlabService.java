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

    final String EXPEDITE_LABEL = "Expedite";

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

    protected Date getDate(String date) throws ParseException {
        return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX").parse(date);
    }

    protected Date getYesterdayDate() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.DAY_OF_MONTH, -1);

        return cal.getTime();
    }

    protected String getProjectName(Integer projectId) throws GitLabApiException {
        return gitLabApi.getProjectApi().getProject(projectId).getName();
    }
}
