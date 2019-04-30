package com.nomad.app.repository;

import com.nomad.app.model.TableInfo;
import org.javatuples.Triplet;

/**
 * @author Md Shariful Islam
 */
public interface DestinationDBDAO {
    boolean createSyncTable(String tableName);
    boolean syncData(boolean isInsert, boolean isDelete, boolean isUpdate);
    boolean dataImport();
    boolean syncInsert(String tableName, String filter, byte[] data);
    boolean syncDelete();
    boolean syncUpdate();
    boolean syncSchedule(String schedule);
    boolean createTable(TableInfo tableInfo);
    boolean initSyncLookupData();
    int getCurrentEventPos();
    Triplet<Boolean, Boolean, Boolean> getSyncConfig();
    // Need to invoke after initialization
    void init();
}
