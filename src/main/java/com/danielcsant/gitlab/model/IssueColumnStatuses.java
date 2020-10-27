package com.danielcsant.gitlab.model;

import org.gitlab4j.api.models.Issue;

import java.util.HashMap;

public class IssueColumnStatuses {
    Issue issue;

    public IssueColumnStatuses(Issue issue) {
        this.issue = issue;
    }

    HashMap<String, ColumnStatus> columnStatusHashMap = new HashMap<>();

    public void addColumnStatus(ColumnStatus columnStatus) {
        columnStatusHashMap.put(columnStatus.columnName, columnStatus);
    }

    public Issue getIssue() {
        return issue;
    }

    public HashMap<String, ColumnStatus> getColumnStatusHashMap() {
        return columnStatusHashMap;
    }

    @Override
    public String toString() {

        StringBuffer stringBuffer = new StringBuffer();

        stringBuffer.append("IssueColumnStatuses{")
                .append("issue=")
                .append(issue);
        for (String name: columnStatusHashMap.keySet()){
            ColumnStatus value = columnStatusHashMap.get(name);
            stringBuffer.append("columnStatus=")
                    .append(value);
        }

        return stringBuffer.toString();
    }
}
