package com.nomad.app.repository;

import groovy.lang.Tuple2;

import java.util.List;
import java.util.Map;

/**
 * @author Md Shariful Islam
 */
public interface TriggerTemplate {
    boolean createEventTable(String tableName);
    List<String> getSyncTableList(String dbName);
    List<Tuple2<String, String>> getColumnInfo(String table);
    List<String> getPrimaryKeys(String catalog, String schema, String table);
    List<Map<String, Object>> getMetadataList(List<String> tableNameList);
    void createTrigger(String tableName);
    void createTriggerList(List<String> tableNameList);
}
