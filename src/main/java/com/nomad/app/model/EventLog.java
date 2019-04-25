package com.nomad.app.model;

import java.sql.Timestamp;
import java.util.Arrays;

/**
 * @author Md Shariful Islam
 */
public class EventLog {
    Integer id;
    String originalTableName;
    String operation;
    String filter;
    byte[] newData;
    byte[] oldData;
    Timestamp createDateTime;
    String status;

    public Integer getId() {
        return id;
    }

    public EventLog setId(Integer id) {
        this.id = id;
        return this;
    }

    public String getOriginalTableName() {
        return originalTableName;
    }

    public EventLog setOriginalTableName(String originalTableName) {
        this.originalTableName = originalTableName;
        return this;
    }

    public String getOperation() {
        return operation;
    }

    public EventLog setOperation(String operation) {
        this.operation = operation;
        return this;
    }

    public String getFilter() {
        return filter;
    }

    public EventLog setFilter(String filter) {
        this.filter = filter;
        return this;
    }

    public byte[] getNewData() {
        return newData;
    }

    public EventLog setNewData(byte[] newData) {
        this.newData = newData;
        return this;
    }

    public byte[] getOldData() {
        return oldData;
    }

    public EventLog setOldData(byte[] oldData) {
        this.oldData = oldData;
        return this;
    }

    public Timestamp getCreateDateTime() {
        return createDateTime;
    }

    public EventLog setCreateDateTime(Timestamp createDateTime) {
        this.createDateTime = createDateTime;
        return this;
    }

    public String getStatus() {
        return status;
    }

    public EventLog setStatus(String status) {
        this.status = status;
        return this;
    }

    @Override
    public String toString() {
        return "EventLog{" +
                "id=" + id +
                ", originalTableName='" + originalTableName + '\'' +
                ", operation='" + operation + '\'' +
                ", filter='" + filter + '\'' +
                ", newData=" + Arrays.toString(newData) +
                ", oldData=" + Arrays.toString(oldData) +
                ", createDateTime=" + createDateTime +
                ", status='" + status + '\'' +
                '}';
    }
}
