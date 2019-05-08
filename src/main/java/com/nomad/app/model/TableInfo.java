package com.nomad.app.model;

import org.javatuples.Pair;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Md Shariful Islam
 */
public class TableInfo {
    Integer id;
    String tableName;
    Map<String, Pair<String, String>> columnMap = new HashMap<>();
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

    public Map<String, Pair<String, String>> getColumnMap() {
        return columnMap;
    }

    public TableInfo setColumnMap(Map<String, Pair<String, String>> columnMap) {
        this.columnMap = columnMap;
        return this;
    }

    public TableInfo setColumnMap(String columnListStr) {
        for (String fragment : columnListStr.split("\\s*,\\s*")) {
            String[] fragmentArray = fragment.split(" ");
            this.columnMap.put(fragmentArray[0], new Pair<>(fragmentArray[1], fragmentArray[2]));
        }
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
                ", columnMap=" + columnMap +
                ", uniqueColumns='" + uniqueColumns + '\'' +
                ", createDate=" + createDate +
                ", status='" + status + '\'' +
                '}';
    }
}
