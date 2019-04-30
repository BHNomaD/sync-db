package com.nomad.app.repository;

import com.nomad.app.core.SyncConfigurer;
import com.nomad.app.model.EnumerationList;
import com.nomad.app.model.EventLog;
import com.nomad.app.model.SinkDBConn;
import com.nomad.app.model.TableInfo;
import org.javatuples.Triplet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author Md Shariful Islam
 */
@Repository
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PostgresDBDAOImpl implements DestinationDBDAO {

    private static final Logger logger = LoggerFactory.getLogger(PostgresDBDAOImpl.class);

    @Autowired
    private BeanFactory beanFactory;

    @Autowired
    CommonDAO commonDAO;

    @Autowired
    SyncConfigurer syncConfigurer;

    private JdbcTemplate sourceJdbc;
    private JdbcTemplate sinkJdbc;
    private FetchDAO fetchDAO;
    private String dbName;

    private int currentEvenetPos = -2;

    private boolean isInsert = true;
    private boolean isDelete = true;
    private boolean isUpdate = true;

    PostgresDBDAOImpl(JdbcTemplate sourceJdbc, SinkDBConn sinkDBConn) {
        this.sourceJdbc = sourceJdbc;
        this.sinkJdbc = sinkDBConn.getJdbc();
        this.dbName = sinkDBConn.getConfig().get(EnumerationList.Proeprties.DB_CONFIG_NAME);

    }

    public void init() {
        fetchDAO = beanFactory.getBean(FetchDAO.class, this.sourceJdbc);
        for (String tableName : syncConfigurer.getSyncTableList(this.dbName, false)) {
            createSyncTable(tableName);
        }
        initSyncLookupData();
        currentEvenetPos = getCurrentEventPos();
    }

    @Override
    public boolean createSyncTable(String tableName) {
        try {
            TableInfo tableInfo = commonDAO.getTableInfo(sourceJdbc, tableName);
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
    public boolean dataImport() {
        //TODO
        List<EventLog> eventLogList = fetchDAO.getEvent(currentEvenetPos,
                Integer.parseInt(syncConfigurer.getPropertiesMap(dbName,false)
                        .get(EnumerationList.Proeprties.SYNC_SIZE.toString())));

        eventLogList.forEach( eventLog -> {
            if(eventLog.getOperation().equalsIgnoreCase(EnumerationList.Operator.INSERT.toString())) {
                syncInsert(eventLog.getOriginalTableName(), eventLog.getFilter(), eventLog.getNewData());
            } else if(eventLog.getOperation().equalsIgnoreCase(EnumerationList.Operator.DELETE.toString())) {
                syncDelete();
            } else if(eventLog.getOperation().equalsIgnoreCase(EnumerationList.Operator.UPDATE.toString())) {
                syncUpdate();
            }
        });

        return true;
    }

    @Override
    public boolean syncInsert(String tableName, String filter, byte[] data) {
        //TODO
        boolean allColumn = false;
        List<String> columnList = syncConfigurer.getSyncColumnList(dbName,tableName,EnumerationList.Operator.INSERT,false);

        String sql = "INSERT INTO " + tableName.toUpperCase() + " VALUES ( ";
        if(columnList == null || columnList.size() == 0) allColumn = true;

        String str = Arrays.toString(data);
        for (String s : str.split("\\s*,\\s*")) {
            String com[] = s.split("\\s*=\\s*");
            if(allColumn == true || columnList.contains(com[0])) {

            }
        }

        return false;
    }

    @Override
    public boolean syncDelete() {
        //TODO
        return false;
    }

    @Override
    public boolean syncUpdate() {

        return false;
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
            sinkJdbc.execute(sql);
        } catch (Exception ex) {
            logger.error("Error creating table from table-info: ", ex);
            return false;
        }
        return true;
    }

    @Override
    public boolean initSyncLookupData() {

        String sql = "CREATE TABLE IF NOT EXISTS LKP_SYNC ( " +
                    "   NAME  VARCHAR(100) PRIMARY KEY, " +
                    "   VALUE VARCHAR(200) NOT NULL, " +
                    "   LAST_UPDATE_TIME TIMESTAMP, " +
                    "   REMARKS  VARCHAR(100) )";

        sinkJdbc.execute(sql);

        sql = "INSERT INTO LKP_SYNC VALUES (?, ?, now(), ? ) ON CONFLICT(NAME) DO UPDATE SET LAST_UPDATE_TIME = now(), REMARKS = ?";
        sinkJdbc.update(sql, EnumerationList.LKPSttings.CURRENT_IMPORT_POSITION.toString(),
                EnumerationList.LKPSttings.CURRENT_IMPORT_POSITION.getValue(),
                EnumerationList.LKPSttings.IMPORT_POSITION_INIT_REMARKS.getValue(),
                EnumerationList.LKPSttings.IMPORT_POSITION_INIT_REMARKS_ON_CONFLICT.getValue());

        return true;
    }

    @Override
    public int getCurrentEventPos() {
        String sql = "SELECT VALUE FROM LKP_SYNC WHERE NAME = ?";
        return Integer.parseInt(sinkJdbc.queryForObject(sql, String.class, EnumerationList.LKPSttings.CURRENT_IMPORT_POSITION.toString()));
    }

    @Override
    public Triplet<Boolean, Boolean, Boolean> getSyncConfig() {
        return new Triplet<>(this.isInsert, this.isDelete, this.isUpdate);
    }
}
