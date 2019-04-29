package com.nomad.app.repository;

import com.nomad.app.model.TableInfo;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * @author Md Shariful Islam
 */
public interface DestinationDBDAO {
    boolean createSyncTable(String tableName);
    boolean syncData(boolean isInsert, boolean isDelete, boolean isUpdate);
    boolean syncSchedule(String schedule);
    boolean createTable(TableInfo tableInfo);
    // Need to invoke after initialization
    void init();
}
