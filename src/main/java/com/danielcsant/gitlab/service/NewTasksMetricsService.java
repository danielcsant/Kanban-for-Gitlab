package com.danielcsant.gitlab.service;

import com.danielcsant.gitlab.model.IssuesInDate;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.models.Issue;

import java.util.*;

public class NewTasksMetricsService extends GitlabService{

    public NewTasksMetricsService(String hostUrl, String personalAccessToken) throws GitLabApiException {
        super(hostUrl, personalAccessToken);
    }


    public List<Issue> getTasksCreatedYesterday(HashMap<String, List<Issue>> columns) throws Exception {
        List<Issue> result = new ArrayList<>();
        for (List<Issue> valuesColumn : columns.values()) {
            for (Issue issue : valuesColumn) {
                if (wasCreatedYesterday(issue)){
                    result.add(issue);
                }
            }
        }

        return result;
    }

    private boolean wasCreatedYesterday(Issue issue) {
        String createdDateString = issue.getCreatedAt().toString();
        String createdDateDay = createdDateString.substring(0, 10);

        Date yesterdayDate = getYesterdayDate();
        String yesterdayDayString = yesterdayDate.toString();
        String yesterdayDayDay = yesterdayDayString.substring(0, 10);

        return createdDateDay.equals(yesterdayDayDay);
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
