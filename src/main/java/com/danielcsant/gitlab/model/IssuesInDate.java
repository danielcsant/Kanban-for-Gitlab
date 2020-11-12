package com.danielcsant.gitlab.model;

import org.gitlab4j.api.models.Issue;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class IssuesInDate implements Comparable<IssuesInDate> {

    private Date created;
    private List<Issue> issues;

    public IssuesInDate(Date created, List<Issue> issues) {
        this.created = created;
        this.issues = issues;
    }

    public Date getCreated() {
        return created;
    }

    public List<Issue> getIssues() {
        return issues;
    }

    @Override
    public int compareTo(IssuesInDate o) {
        return created.compareTo(o.getCreated());
    }
}
