package com.danielcsant.gitlab.service;

import com.danielcsant.gitlab.model.ColumnStatus;
import com.danielcsant.gitlab.model.IssueColumnStatuses;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.models.Issue;
import org.gitlab4j.api.models.LabelEvent;
import org.gitlab4j.api.models.Project;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class GitlabService {

    GitLabApi gitLabApi = null;
    List<Project> projects = null;
    int closedAtStart = 0;

    public GitlabService(String hostUrl, String personalAccessToken, int closedAtStart) throws GitLabApiException {
        // Create a GitLabApi instance to communicate with your GitLab server
        gitLabApi = new GitLabApi(hostUrl, personalAccessToken);

        // Get the list of projects your account has access to
        projects = gitLabApi.getProjectApi().getProjects();

        this.closedAtStart = closedAtStart;
    }

    public HashMap<String, List<Issue>> getColumnsMap(String projectName, String[] columnNames) throws Exception {
        Project project = getProject(projectName);

        HashMap<String, List<Issue>> columns = new HashMap<>();
        for (int i = 0; i < columnNames.length; i++) {
            String column = columnNames[i];
            columns.put(column, new ArrayList<>());
        }

        List<Issue> openedIssues = new ArrayList<>();
        List<Issue> reopenedIssues = new ArrayList<>();
        List<Issue> closedIssues = new ArrayList<>();
        // Get a list of issues for the specified project ID
        List<Issue> issues = gitLabApi.getIssuesApi().getIssues(project.getId());
        for (Issue issue : issues) {
            switch (issue.getState()) {
                case OPENED:
                    try {
                        boolean hasColumnLabel = false;
                        if (issue.getLabels() != null && issue.getLabels().size() > 0){
                            for (String label : issue.getLabels()) {
                                if (columns.containsKey(label)){
                                    hasColumnLabel = true;
                                    break;
                                }
                            }
                        }
                        if (!hasColumnLabel){
                            openedIssues.add(issue);
                        }
                    } catch (Exception e){
                        if (issue.getLabels() != null && issue.getLabels().size() > 0){
                            System.err.println(issue.getLabels());
                        }
                        throw e;
                    }
                    break;
                case CLOSED:
                    closedIssues.add(issue);
                    break;
                case REOPENED:
                    reopenedIssues.add(issue);
                    break;
            }

            switch (issue.getState()) {
                case OPENED:
                case REOPENED:
                    try {
                        if (issue.getLabels() != null && issue.getLabels().size() > 0){
                            for (String label : issue.getLabels()) {
                                if (columns.containsKey(label)){
                                    columns.get(label).add(issue);
                                }
                            }
                        }
                    } catch (Exception e){
                        if (issue.getLabels() != null && issue.getLabels().size() > 0){
                            System.err.println(issue.getLabels());
                        }
                        throw e;
                    }

                    break;
            }

        }

        columns.put("Open", openedIssues);
        columns.put("Closed", closedIssues);
        columns.put("Reopened", reopenedIssues);

        for (String s : columnNames) {
            System.out.println("Issues in column " + s + ": " + columns.get(s).size());
        }

        return columns;
    }

    private Project getProject(String projectName) throws Exception {
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

    public List<IssueColumnStatuses> getIssuesStatuses(String projectName, HashMap<String, List<Issue>> columns, String[] columnNames) throws Exception {

        List<IssueColumnStatuses> issuesStatuses = new ArrayList<>();

        for (int i = 0; i < columnNames.length; i++) {
            String columnName = columnNames[i];
            List<Issue> issuesInColumn = columns.get(columnName);
            if (issuesInColumn == null) {
                continue;
            }

            Project project = getProject(projectName);

            for (Issue issue : issuesInColumn) {
                List<LabelEvent> labelEvents = gitLabApi
                        .getResourceLabelEventsApi()
                        .getIssueLabelEvents(project.getId(), issue.getIid());

                if (labelEvents != null && !labelEvents.isEmpty()){
                    IssueColumnStatuses issueColumnStatuses = getIssueColumnStatuses(columnNames, issue, labelEvents);
                    issuesStatuses.add(issueColumnStatuses);
                }
            }
        }

        return issuesStatuses;

    }

    private IssueColumnStatuses getIssueColumnStatuses(String[] columnNames, Issue issue, List<LabelEvent> labelEvents) throws ParseException {
        IssueColumnStatuses issueColumnStatuses = new IssueColumnStatuses(issue);
        for (int j = 0; j < columnNames.length; j++) {
            String name = columnNames[j];
            List<LabelEvent> labelEventsByColumn = getLabelEventsByColumnName(name, labelEvents);
            if (labelEventsByColumn != null && labelEventsByColumn.size() > 0){
                ColumnStatus columnStatus = new ColumnStatus(name);
                for (LabelEvent labelEvent : labelEventsByColumn) {
                    if (labelEvent.getAction().equalsIgnoreCase("add")){
                        if (isAddedDateEarlier(columnStatus, getDate(labelEvent.getCreatedAt()))) {
                            columnStatus.setAddedDate(getDate(labelEvent.getCreatedAt()));
                        }
                    } else if (labelEvent.getAction().equalsIgnoreCase("remove")){
                        if (isRemovedDateLatest(columnStatus, getDate(labelEvent.getCreatedAt()))) {
                            columnStatus.setRemovedDate(getDate(labelEvent.getCreatedAt()));
                        }
                    } else {
                        System.out.println("Action label not recognised: " + labelEvent.getAction());
                    }
                }
                issueColumnStatuses.addColumnStatus(columnStatus);
            }
        }
        return issueColumnStatuses;
    }

    private boolean isRemovedDateLatest(ColumnStatus columnStatus, Date date) {
        boolean isRemovedDateLatest = false;
        Date removedDate = columnStatus.getRemovedDate();
        if (removedDate == null || (removedDate != null && date.after(removedDate))) {
            isRemovedDateLatest = true;
        }

        return isRemovedDateLatest;    }

    private boolean isAddedDateEarlier(ColumnStatus columnStatus, Date date) {

        boolean isAddedDateEarlier = false;
        Date addedDate = columnStatus.getAddedDate();
        if (addedDate == null || (addedDate != null && date.before(addedDate))) {
            isAddedDateEarlier = true;
        }

        return isAddedDateEarlier;
    }

    private List<LabelEvent> getLabelEventsByColumnName(String name, List<LabelEvent> labelEvents) {
        List<LabelEvent> labelEventsForColumn = new ArrayList<>();

        for (LabelEvent labelEvent : labelEvents) {
            if (labelEvent.getLabel() != null &&
                    labelEvent.getLabel().getName() != null &&
                    labelEvent.getLabel().getName().equalsIgnoreCase(name)){
                labelEventsForColumn.add(labelEvent);
            }
        }

        return labelEventsForColumn;
    }

    public Date getDate(String date) throws ParseException {
        return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX").parse(date);
    }

    public Issue getIssue(String projectName, int iId) throws Exception {
        return gitLabApi.getIssuesApi().getIssue(getProject(projectName), iId);
    }
}
