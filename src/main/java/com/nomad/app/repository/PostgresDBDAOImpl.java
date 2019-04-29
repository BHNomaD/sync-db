package com.nomad.app.repository;

import com.nomad.app.core.SyncConfigurer;
import com.nomad.app.model.EnumerationList;
import com.nomad.app.model.SinkDBConn;
import com.nomad.app.model.TableInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * @author Md Shariful Islam
 */
@Repository
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PostgresDBDAOImpl implements DestinationDBDAO {

    private static final Logger logger = LoggerFactory.getLogger(PostgresDBDAOImpl.class);

    @Autowired
    @Qualifier("jdbc-03")
    JdbcTemplate jdbcTemplate03;

    @Autowired
    CommonDAO commonDAO;

    @Autowired
    SyncConfigurer syncConfigurer;

    private JdbcTemplate sinkJdbcTemplate;
    private String dbName;

    private boolean isInsert = true;
    private boolean isDelete = true;
    private boolean isUpdate = true;

    PostgresDBDAOImpl(SinkDBConn sinkDBConn) {
        this.sinkJdbcTemplate = sinkDBConn.getJdbc();
        this.dbName = sinkDBConn.getConfig().get(EnumerationList.Proeprties.DB_CONFIG_NAME);
    }

    public void init() {
        for (String tableName : syncConfigurer.getSyncTableList(this.dbName, false)) {
            createSyncTable(tableName);
        }
    }

    @Override
    public boolean createSyncTable(String tableName) {
        try {
            TableInfo tableInfo = commonDAO.getTableInfo(jdbcTemplate03, tableName);
            createTable(tableInfo);
        } catch (Exception ex) {
            logger.error("Error creating sink-table", ex);
            return false;
        }

        return true;
    }

    @Override
    public boolean syncData(boolean isInsert, boolean isDelete, boolean isUpdate) {
        this.isInsert = isInsert;
        this.isDelete = isDelete;
        this.isUpdate = isUpdate;
        return true;
    }

    @Override
    public boolean syncSchedule(String schedule) {
        return false;
    }

    @Override
    public boolean createTable(TableInfo tableInfo) {

        try {
            logger.info("Creating sql for create sink table {}", tableInfo.getTableName());
            String sql = " CREATE TABLE IF NOT EXISTS " + tableInfo.getTableName().toUpperCase() + " ( ";
            for (String columnInfo : tableInfo.getColumnList().split("\\s*,\\s*")) {
                String tmp[] = columnInfo.split("\\s+");
                sql = sql + " " + tmp[0].toUpperCase() + " ";
                if (tmp[1].toUpperCase().equalsIgnoreCase("VARCHAR2") || tmp[1].toUpperCase().equalsIgnoreCase("VARCHAR")) {
                    sql = sql + " " + "VARCHAR" + "(" + tmp[2] + "), ";
                } else if (tmp[1].toUpperCase().equalsIgnoreCase("NUMBER") && Integer.parseInt(tmp[2]) < 10) {
                    sql = sql + " " + "INT" + ", ";
                } else if (tmp[1].toUpperCase().equalsIgnoreCase("NUMBER") && Integer.parseInt(tmp[2]) > 9) {
                    sql = sql + " " + "BIGINT" + ", ";
                } else {
                    sql = sql + " " + tmp[1] + ", ";
                }
            }
            sql = sql.substring(0, sql.length() - 2) + " ) ";

            logger.info("Preparing to execute create-table {}", tableInfo.getTableName());
            sinkJdbcTemplate.execute(sql);
        } catch (Exception ex) {
            logger.error("Error creating table from table-info: ", ex);
            return false;
        }
        return true;
    }

}
