package com.nomad.app.repository;

import groovy.lang.Tuple2;

import java.util.List;
import java.util.Map;

/**
 * @author Md Shariful Islam
 */
public interface TriggerTemplate {
    void process();
    boolean createEventTable();
    List<String> getSyncTableList(String dbName);
    List<Tuple2<String, String>> getColumnInfo(String table);
    List<String> getPrimaryKeys(String table);
    void createInsertTriggerEachRow(String tableName, List<String> columnList, List<String> primaryKeys);
    void createDeleteTriggerEachRow(String tableName, List<String> columnList, List<String> primaryKeys);
    void createUpdateTriggerEachRow(String tableName, List<String> columnList, List<String> primaryKeys);
}
