package com.nomad.app.service;

import java.util.List;
import java.util.Map;

/**
 * @author Md Shariful Islam
 */
public class OracleTriggerImpl implements TriggerTemplate {

    @Override
    public boolean createEventTable(String tableName) {
        return false;
    }

    @Override
    public List<String> getSyncTableList() {
        return null;
    }

    @Override
    public Map<String, Object> getMetadata(String tableName) {
        return null;
    }

    @Override
    public List<Map<String, Object>> getMetadataList(List<String> tableNameList) {
        return null;
    }

    @Override
    public void createTrigger(String tableName) {

    }

    @Override
    public void createTriggerList(List<String> tableNameList) {

    }
}
