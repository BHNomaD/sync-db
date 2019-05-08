package com.nomad.app.repository;

import org.javatuples.Triplet;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

/**
 * @author Md Shariful Islam
 */
public interface TriggerTemplate {
    void init(String dbName, JdbcTemplate jdbcTemplate);
    void process();
    boolean createEventTable();
    List<String> getSyncTableList(String dbName);
    List<Triplet<String, String, Integer>> getColumnInfo(String table);
    List<String> getPrimaryKeys(String table);
    void createInsertTriggerEachRow(String tableName, List<String> columnList, List<String> primaryKeys);
    void createDeleteTriggerEachRow(String tableName, List<String> columnList, List<String> primaryKeys);
    void createUpdateTriggerEachRow(String tableName, List<String> columnList, List<String> primaryKeys);
}
