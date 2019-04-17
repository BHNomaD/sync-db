package com.nomad.app.service;

import java.util.List;
import java.util.Map;

/**
 * @author Md Shariful Islam
 */
public interface TriggerTemplate {
    boolean createEventTable(String tableName);
    List<String> getSyncTableList();
    Map<String, Object> getMetadata(String tableName);
    List<Map<String, Object>> getMetadataList(List<String> tableNameList);
    void createTrigger(String tableName);
    void createTriggerList(List<String> tableNameList);
}
