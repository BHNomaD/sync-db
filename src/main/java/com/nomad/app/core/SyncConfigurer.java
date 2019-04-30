package com.nomad.app.core;

import com.nomad.app.model.EnumerationList;
import org.javatuples.Triplet;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Md Shariful Islam
 */
public interface SyncConfigurer {
    Map<String, String> getPropertiesMap(String db, boolean isRefresh);
    List<String> getSyncDBList(boolean isRefresh);
    Set<String> getSyncDBSet(boolean isRefresh);
    List<String> getSyncTableList(String dbName, boolean isRefresh);
    List<String> getSyncColumnList(String dbName, String tableName, EnumerationList.Operator op, boolean isRefresh);
    void storeSyncColumnList(String dbName);
    Map<Triplet<String, String, String>, List<String>> getAllColumnList();
}
