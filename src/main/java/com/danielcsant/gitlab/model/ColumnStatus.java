package com.danielcsant.gitlab.model;

import java.util.Date;

public class ColumnStatus {
    String columnName;
    Date addedDate;
    Date removedDate;

    public ColumnStatus(String name) {
        this.columnName = name;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public Date getAddedDate() {
        return addedDate;
    }

    public void setAddedDate(Date addedDate) {
        this.addedDate = addedDate;
    }

    public Date getRemovedDate() {
        return removedDate;
    }

    public void setRemovedDate(Date removedDate) {
        this.removedDate = removedDate;
    }

    @Override
    public String toString() {
        return "ColumnStatus{" +
                "columnName='" + columnName + '\'' +
                ", addedDate=" + addedDate +
                ", removedDate=" + removedDate +
                '}';
    }
}
