package com.nomad.app.repository;

import com.nomad.app.model.TableInfo;
import org.javatuples.Triplet;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * @author Md Shariful Islam
 */
public interface DestinationDBDAO {
    boolean createSyncTable(String tableName);
    boolean syncData(boolean isInsert, boolean isDelete, boolean isUpdate);
    boolean dataImport();
    boolean syncInsert(String tableName, byte[] data);
    boolean syncDelete(String tableName, String uniqueColumns);
    boolean syncUpdate(String tableName, String filter, byte[] data);
    boolean syncSchedule(String schedule);
    boolean createTable(TableInfo tableInfo);
    boolean initSyncLookupData();
    int getCurrentEventPos();
    Triplet<Boolean, Boolean, Boolean> getSyncConfig();
    // Need to invoke after initialization
    void init(JdbcTemplate sourceJdbc, JdbcTemplate sinkJdbc, String sourceDBName, String sinkDBName);
}
