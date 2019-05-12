package com.nomad.app.repository;

import com.nomad.app.core.SyncConfigurer;
import com.nomad.app.model.EnumerationList;
import com.nomad.app.model.EventLog;
import com.nomad.app.model.TableInfo;
import org.javatuples.Triplet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
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
    private String sinkDBName;
    private String sourceDBName;

    private int currentEvenetPos = -2;

    private boolean isInsert = true;
    private boolean isDelete = true;
    private boolean isUpdate = true;

    private Map<String, String> insertSQLList = new HashMap<>();
    private Map<String, String> deleteSQLList = new HashMap<>();


    @Override
    public void init(JdbcTemplate sourceJdbc, JdbcTemplate sinkJdbc, String sourceDBName, String sinkDBName) {
        this.sourceJdbc = sourceJdbc;
        this.sinkJdbc = sinkJdbc;
        this.sinkDBName = sinkDBName;
        this.sourceDBName = sourceDBName;

        fetchDAO = beanFactory.getBean(FetchDAO.class, this.sourceJdbc);
        for (String tableName : syncConfigurer.getSyncTableList(this.sinkDBName, false)) {
            createSyncTable(tableName);
            prepareInsertSQL(tableName);
            prepareDeleteSQL(tableName);
        }
        initSyncLookupData();
        currentEvenetPos = getCurrentEventPos();
    }

    @Override
    public boolean createSyncTable(String tableName) {
        try {
            TableInfo tableInfo = commonDAO.getTableInfo(sourceJdbc, sourceDBName, tableName);
            sourceJdbc.getDataSource().getConnection().getCatalog();
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
        logger.info("in data-import");
        List<EventLog> eventLogList = fetchDAO.getEvent(currentEvenetPos,
                Integer.parseInt(syncConfigurer.getPropertiesMap(sinkDBName,false)
                        .get(EnumerationList.Proeprties.SYNC_SIZE.toString())));
        logger.info("Event log fetched :: {}", eventLogList.size());
        eventLogList.forEach( eventLog -> {
            if(eventLog.getOperation().equalsIgnoreCase(EnumerationList.Operator.INSERT.toString())) {
                syncInsert(eventLog.getOriginalTableName(), eventLog.getNewData());
            } else if(eventLog.getOperation().equalsIgnoreCase(EnumerationList.Operator.DELETE.toString())) {
                syncDelete(eventLog.getOriginalTableName(),eventLog.getFilter());
            } else if(eventLog.getOperation().equalsIgnoreCase(EnumerationList.Operator.UPDATE.toString())) {
                syncUpdate(eventLog.getOriginalTableName(), eventLog.getFilter(), eventLog.getNewData());
            }
        });

        if(eventLogList.size() > 0) {
            currentEvenetPos = eventLogList.get(eventLogList.size() - 1).getId();
            sinkJdbc.update("UPDATE LKP_SYNC SET VALUE = ? WHERE NAME = ?", currentEvenetPos, "CURRENT_IMPORT_POSITION");
        }
        return true;
    }

    @Override
    public boolean syncInsert(String tableName, byte[] data) {
        Map<String, Object> dataMap = getByteArrayToDataMap(tableName, data);
        List<Object> insertObjectList = prepareObjectList(dataMap,
                syncConfigurer.getSyncColumnList(sinkDBName,tableName,EnumerationList.Operator.INSERT,false));
        sinkJdbc.update(insertSQLList.get(tableName), insertObjectList.toArray());
        return true;
    }

    @Override
    public boolean syncDelete(String tableName, String filter) {
        Map<String, Object> dataMap = getStringToDataMap(tableName, filter);
        List<Object> filterObjectList = prepareObjectList(dataMap,
                syncConfigurer.getSyncColumnList(sinkDBName,tableName,EnumerationList.Operator.DELETE,false));
        sinkJdbc.update(deleteSQLList.get(tableName), filterObjectList.toArray());
        return true;
    }

    @Override
    public boolean syncUpdate(String tableName, String filter, byte[] data) {

        var ref = new Object() {
            String prefix = "";
            String suffix = "";
            List<Object> objectList = new ArrayList<>();
        };

        Map<String, Object> setDataMap = getByteArrayToDataMap(tableName, data);
        Map<String, Object> filterDataMap = getStringToDataMap(tableName, filter);

        setDataMap.forEach( (name, value) -> {
            ref.prefix = ref.prefix + ", " + name + " = ?";
            ref.objectList.add(value);
        });
        filterDataMap.forEach( (name, value) -> {
            ref.suffix = ref.suffix + ", " + name + " = ?";
            ref.objectList.add(value);
        });

        String sql = "UPDATE " + tableName.toUpperCase() + " SET " + ref.prefix.substring(1) + " WHERE " + ref.suffix.substring(1);
        sinkJdbc.update(sql, ref.objectList.toArray());

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
            var ref = new Object() {
                String sql = " CREATE TABLE IF NOT EXISTS " + tableInfo.getTableName().toUpperCase() + " ( ";
            };

            tableInfo.getColumnMap().forEach( (name, desc) -> {
                ref.sql = ref.sql + " " + name.toUpperCase() + " ";

                if (desc.getValue0().toUpperCase().equalsIgnoreCase("VARCHAR2") || desc.getValue0().toUpperCase().equalsIgnoreCase("VARCHAR")) {
                    ref.sql = ref.sql + " " + "VARCHAR" + "(" + desc.getValue1() + "), ";
                } else if (desc.getValue0().toUpperCase().equalsIgnoreCase("NUMBER") && Integer.parseInt(desc.getValue1()) < 10) {
                    ref.sql = ref.sql + " " + "INT" + ", ";
                } else if (desc.getValue0().toUpperCase().equalsIgnoreCase("NUMBER") && Integer.parseInt(desc.getValue1()) > 9) {
                    ref.sql = ref.sql + " " + "BIGINT" + ", ";
                } else {
                    ref.sql = ref.sql + " " + desc.getValue0() + ", ";
                }
            });
            ref.sql = ref.sql.substring(0, ref.sql.length() - 2) + " ) ";

            logger.info("Preparing to execute create-table {}", tableInfo.getTableName());
            sinkJdbc.execute(ref.sql);
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

    private Map<String, Object> getByteArrayToDataMap(String tableName, byte[] data) {
        String dataStr = new String(data);
        return getStringToDataMap(tableName, dataStr);
    }

    private Map<String, Object> getStringToDataMap(String tableName, String dataStr) {

        Map<String, Object> retMap = new HashMap<>();
        TableInfo tableInfo = syncConfigurer.getTableInfo(this.sourceDBName, tableName);

        for(String fragment : dataStr.split("\\s*,\\s*")) {
            String[] fragmentArray = fragment.split("\\s*>\\s*");

            Object object = null;
            String columnType = tableInfo.getColumnMap().get(fragmentArray[0]).getValue0();
            if( columnType.equalsIgnoreCase("VARCHAR") || columnType.equalsIgnoreCase("VARCHAR2")) {
                object = fragmentArray[1];
            } else if(columnType.equalsIgnoreCase("INT")) {
                object = Integer.parseInt(fragmentArray[1]);
            } else if(columnType.equalsIgnoreCase("NUMBER")) {
                object = new BigInteger(fragmentArray[1]);
            } else if(columnType.equalsIgnoreCase("BLOB") || columnType.equalsIgnoreCase("CLOB")) {
                object = fragmentArray[1].getBytes();
            }
            retMap.put(fragmentArray[0], object);
        }
        return retMap;
    }

    private boolean prepareInsertSQL(String tableName) {
        List<String> columnList = syncConfigurer.getSyncColumnList(sinkDBName,tableName,EnumerationList.Operator.INSERT,false);

        String sql = "INSERT INTO " + tableName.toUpperCase();
        String prefix = "";
        String suffix = "";
        for (String column : columnList) {
            prefix = prefix + ", " + column;
            suffix = suffix + ", " + "?";
        }
        sql = sql + "( " + prefix.substring(1) + ") " + " VALUES ( " + suffix.substring(1) + ")";
        insertSQLList.put(tableName, sql);
        return true;
    }

    private boolean prepareDeleteSQL(String tableName) {

        String sql = "DELETE FROM " + tableName.toUpperCase() + " WHERE ";
        String suffix = "";
        for (String column : syncConfigurer.getTableInfo(this.sourceDBName, tableName).getUniqueColumns().split("\\s*,\\s*")) {
            suffix = suffix + ", " + column + " = ?";
        }
        sql = sql + " " + suffix.substring(1);

        deleteSQLList.put(tableName, sql);
        return true;
    }

    private boolean prepareUpdateSQL(String tableName) {
        //TODO
        // May not needed
        return true;
    }

    private List<Object> prepareObjectList(Map<String, Object> dataMap, List<String> columnList) {
        List<Object> objectList = new ArrayList<>();
        for (String column : columnList) {
            if(dataMap.containsKey(column)) {
                objectList.add(dataMap.get(column));
            } else {
                objectList.add(null);
            }
        }
        return objectList;
    }
}
