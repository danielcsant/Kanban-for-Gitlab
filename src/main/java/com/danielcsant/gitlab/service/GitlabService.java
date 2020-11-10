package com.danielcsant.gitlab.service;

import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.models.Project;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public abstract class GitlabService {

    GitLabApi gitLabApi = null;
    List<Project> projects = null;
    int closedAtStart = 0;

    public GitlabService(String hostUrl, String personalAccessToken, int closedAtStart) throws GitLabApiException {
        // Create a GitLabApi instance to communicate with your GitLab server
        gitLabApi = new GitLabApi(hostUrl, personalAccessToken);

        // Get the list of projects your account has access to
        getProjectList();

        this.closedAtStart = closedAtStart;
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
}
