package com.nomad.app.model;

import java.sql.Timestamp;

/**
 * @author Md Shariful Islam
 */
public class TableInfo {
    Integer id;
    String tableName;
    String columnList;
    String uniqueColumns;
    Timestamp createDate;
    String status;


    public Integer getId() {
        return id;
    }

    public TableInfo setId(Integer id) {
        this.id = id;
        return this;
    }

    public String getTableName() {
        return tableName;
    }

    public TableInfo setTableName(String tableName) {
        this.tableName = tableName;
        return this;
    }

    public String getColumnList() {
        return columnList;
    }

    public TableInfo setColumnList(String columnList) {
        this.columnList = columnList;
        return this;
    }

    public String getUniqueColumns() {
        return uniqueColumns;
    }

    public TableInfo setUniqueColumns(String uniqueColumns) {
        this.uniqueColumns = uniqueColumns;
        return this;
    }

    public Timestamp getCreateDate() {
        return createDate;
    }

    public TableInfo setCreateDate(Timestamp createDate) {
        this.createDate = createDate;
        return this;
    }

    public String getStatus() {
        return status;
    }

    public TableInfo setStatus(String status) {
        this.status = status;
        return this;
    }

    @Override
    public String toString() {
        return "TableInfo{" +
                "id=" + id +
                ", tableName='" + tableName + '\'' +
                ", columnList='" + columnList + '\'' +
                ", uniqueColumns='" + uniqueColumns + '\'' +
                ", createDate=" + createDate +
                ", status='" + status + '\'' +
                '}';
    }
}
