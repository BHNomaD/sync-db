package com.nomad.app.core;

import com.nomad.app.model.EnumerationList;
import com.nomad.app.model.TableInfo;
import org.javatuples.Triplet;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Md Shariful Islam
 */
public interface SyncConfigurer {
    Map<String, String> getPropertiesMap(String db, boolean isRefresh);
    Set<String> getSourceDBSet(boolean isRefresh);
    Set<String> getSinkDBSet(boolean isRefresh);
    Map<String, List<String>> getSourceToSinkMap();
    Map<String, String> getSinkToSourceMap();
    List<String> getSyncTableList(String dbName, boolean isRefresh);
    List<String> getSyncColumnList(String dbName, String tableName, EnumerationList.Operator op, boolean isRefresh);
    void storeSyncColumnList(String dbName);
    Map<Triplet<String, String, String>, List<String>> getAllColumnList();
    TableInfo getTableInfo(String dbName, String tableName);
    void addTableInfo(String dbName, String tableName, TableInfo tableInfo);
}
