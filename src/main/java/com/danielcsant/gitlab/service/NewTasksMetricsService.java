package com.danielcsant.gitlab.service;

import com.danielcsant.gitlab.model.IssuesInDate;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.models.Issue;
import org.gitlab4j.api.models.Project;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class NewTasksMetricsService extends GitlabService{

    public NewTasksMetricsService(String hostUrl, String personalAccessToken, int closedAtStart) throws GitLabApiException {
        super(hostUrl, personalAccessToken, closedAtStart);
    }


    public List<Issue> getTasksCreatedYesterday(String projectName, HashMap<String, List<Issue>> columns) throws Exception {
        Project project = getProject(projectName);
        List<Issue> result = new ArrayList<>();
        for (List<Issue> valuesColumn : columns.values()) {
            for (Issue issue : valuesColumn) {
                if (wasCreatedInLastLaborDay(issue)){
                    result.add(issue);
                }
            }
        }

        return result;
    }

    private boolean wasCreatedInLastLaborDay(Issue issue) {
        String createdDateString = issue.getCreatedAt().toString();
        String createdDateDay = createdDateString.substring(0, 10);

        Date previousWorkingDay = getPreviousWorkingDay();
        String previousWorkingDayString = previousWorkingDay.toString();
        String previousWorkingDayDay = previousWorkingDayString.substring(0, 10);

        return createdDateDay.equals(previousWorkingDayDay);
    }

    public List<IssuesInDate> getCreatedIssues(HashMap<String, List<Issue>> columns) {
        HashMap<Date, List<Issue>> hashMap = new HashMap<>();
        List<IssuesInDate> result = new ArrayList<>();
        for (List<Issue> valuesColumn : columns.values()) {
            for (Issue issue : valuesColumn) {
                Date createdDate = trim(issue.getCreatedAt());
                if (!hashMap.containsKey(createdDate)) {
                    List<Issue> list = new ArrayList<Issue>();
                    list.add(issue);
                    hashMap.put(createdDate, list);
                } else {
                    hashMap.get(createdDate).add(issue);
                }
            }
        }

        for (Date date : hashMap.keySet()) {
            result.add(new IssuesInDate(date, hashMap.get(date)));
        }

        Collections.sort(result);

        return result;
    }


    public Date trim(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.HOUR_OF_DAY, 0);

        return calendar.getTime();
    }

}
