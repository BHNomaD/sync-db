package com.nomad.app.repository;

import com.nomad.app.model.EnumerationList;
import com.zaxxer.hikari.HikariDataSource;
import org.javatuples.Triplet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author Md Shariful Islam
 */
@Service
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class OracleTriggerImpl implements TriggerTemplate {

    private static final Logger logger = LoggerFactory.getLogger(OracleTriggerImpl.class);

    @Autowired
    private Environment env;

    @Autowired
    CommonDAO commonDAO;

    private JdbcTemplate jdbcTemplate;
    private String schema;
    private String catalog;
    private String eventLogTableName;
    private String syncTableInfo;
    private String syncTableList;
    private String eventLogColumnNames;
    private boolean writeTriggerToFileOnly;
    private BufferedWriter writer;

    @Override
    public void init(String dbName, JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.schema = env.getProperty(dbName + "." + EnumerationList.Proeprties.SCHEMA.getValue());
        this.catalog = env.getProperty(dbName + "." + EnumerationList.Proeprties.CATALOG.getValue());
        this.eventLogTableName = env.getProperty(dbName + "." + EnumerationList.Proeprties.EVENT_LOG_TABLE_NAME.getValue());
        this.syncTableInfo = env.getProperty(dbName + "." + EnumerationList.Proeprties.SYNC_TABLE_INFO.getValue());
        this.syncTableList = env.getProperty(dbName + "." + EnumerationList.Proeprties.SYNC_TABLE_LIST.getValue());
        this.eventLogColumnNames = env.getProperty(dbName + "." + EnumerationList.Proeprties.EVENT_LOG_COLUMN_LIST.getValue());
        this.writeTriggerToFileOnly = false;
    }

    public void writeTrigger(String url, String userName, String password, String tableList, BufferedWriter writer) throws IOException {

        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setDriverClassName("oracle.jdbc.OracleDriver");
        dataSource.setJdbcUrl(url);
        dataSource.setUsername(userName);
        dataSource.setPassword(password);
        dataSource.setAutoCommit(true);
        dataSource.setMaximumPoolSize(10);

        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.jdbcTemplate.setFetchSize(100);

        this.schema = userName;
        this.catalog = "";
        this.eventLogTableName = "EVENT_LOG";
        this.syncTableInfo = "SYNC_TABLE_INFO";
        this.syncTableList = tableList;
        this.eventLogColumnNames = "ID,ORIGINAL_TABLE_NAME,OPERATION,FILTER,NEW_DATA,OLD_DATA,CREATE_DATE_TIME,STATUS";
        this.writeTriggerToFileOnly = true;
        this.writer = writer;
        this.commonDAO = new CommonDAOImpl();
    }

    @Override
    public void process(){
//        CREATE TABLE TO STORE SYNC TABLE INFO
        createSyncInfoTable(syncTableInfo);

//        CREATE IF NOT EXISTS EVENT TABLE
        createEventTable();

//        GET ALL TABLE NAME TO BE SYNC
//        syncTableList

//        GET COLUMN NAMES OF THE TABLES
//        GET PRIMARY KEYS OF THE TABLES
//        CREATE INSERT, UPDATE, DELETE TRIGGER TO THE TABLES
        for ( String tableName : syncTableList.split("\\s*,\\s*")) {
            List<String> columnList = new ArrayList<>();
            String columnWithType = "";
            for ( Triplet<String, String, Integer> col : getColumnInfo(tableName)) {
                columnList.add(col.getValue0());
                columnWithType = columnWithType + ", " + col.getValue0() + " " + col.getValue1() + " " + col.getValue2();
            }
            columnWithType = columnWithType.substring(2);
            List<String> primaryKeys = getPrimaryKeys(tableName);

            storeSyncTableInfo(tableName, columnWithType, String.join(", ", primaryKeys));

            createInsertTriggerEachRow(tableName, columnList, primaryKeys);
            createDeleteTriggerEachRow(tableName, columnList, primaryKeys);
            createUpdateTriggerEachRow(tableName, columnList, primaryKeys);
        }
    }

    @Override
    public boolean createEventTable() {

        logger.info("Preparing sql for EVENT_LOG table");

        String sql = " CREATE TABLE EVENT_LOG ( " +
                    "   ID NUMBER NOT NULL PRIMARY KEY, " +
                    "   ORIGINAL_TABLE_NAME VARCHAR2(100) NOT NULL, " +
                    "   OPERATION VARCHAR2(10) NOT NULL, " +
                    "   FILTER VARCHAR2(200) NOT NULL, " +
                    "   NEW_DATA VARCHAR2(200), " +
                    "   OLD_DATA VARCHAR2(200), " +
                    "   CREATE_DATE_TIME TIMESTAMP(6) NOT NULL, " +
                    "   STATUS VARCHAR2(50) NOT NULL " +
                    ")";

        return createTableWithSeq(eventLogTableName, sql);
    }

    @Override
    public List<String> getSyncTableList(String dbName) {
        String[] syncTableList = env.getRequiredProperty(dbName + ".sync-table-list").split(",");
        return Arrays.asList(syncTableList);
    }

    @Override
    public List<Triplet<String, String, Integer>> getColumnInfo(String table) {
        List<Triplet<String, String, Integer>> columnInfos = commonDAO.getColumnInfo(jdbcTemplate, table);
        return columnInfos;
    }

    @Override
    public List<String> getPrimaryKeys(String table) {
        List<Map<String, String>> primaryKeys = commonDAO.getPrimaryKeys(jdbcTemplate, catalog, schema, table);
        List<String> pkIds = new ArrayList<>();
        primaryKeys.forEach( x -> pkIds.add(x.get(EnumerationList.PrimaryKeys.COLUMN_NAME.toString())));
        return pkIds;
    }

    @Override
    public void createInsertTriggerEachRow(String tableName, List<String> columnList, List<String> primaryKeys) {

        String filter = genOldNewColumToStore(primaryKeys, "new");
        String newValues = genOldNewColumToStore(columnList, "new");

        String name = "TRIGGER" + "_INSERT_" + tableName.toUpperCase() + " ";
        String statement = " AFTER INSERT ON " + tableName.toUpperCase() + " ";
        String restriction = " ";
        String actionWhen = " FOR EACH ROW ";
        String actionBody = " BEGIN " +
                            " INSERT INTO " + eventLogTableName + " ( " + eventLogColumnNames + " ) " +
                            " VALUES " + " ( " +
                eventLogTableName.toUpperCase() + "_PK_SEQ.nextval, " + " \'" + tableName.toUpperCase() + "\', \'INSERT\', " +
                filter + ", " + newValues + ", NULL, current_timestamp, \'active\'" +
                            " ); " +
                            " END; ";

        writeORInsertSQL(name, statement, restriction, actionWhen, actionBody);
    }

    @Override
    public void createDeleteTriggerEachRow(String tableName, List<String> columnList, List<String> primaryKeys) {

        String filter = genOldNewColumToStore(primaryKeys, "old");
        String oldValues = genOldNewColumToStore(columnList, "old");

        String name = "TRIGGER" + "_DELETE_" + tableName.toUpperCase() + " ";
        String statement = " AFTER DELETE ON " + tableName.toUpperCase() + " ";
        String restriction = " ";
        String actionWhen = " FOR EACH ROW ";
        String actionBody = " BEGIN " +
                            " INSERT INTO " + eventLogTableName + " ( " + eventLogColumnNames + " ) " +
                            " VALUES " + " ( " +
                eventLogTableName.toUpperCase() + "_PK_SEQ.nextval, " + " \'" + tableName.toUpperCase() + "\', \'DELETE\', " +
                filter + ", NULL, " + oldValues + ", current_timestamp, \'active\'" +
                            " ); " +
                            " END; ";

        writeORInsertSQL(name, statement, restriction, actionWhen, actionBody);
    }

    @Override
    public void createUpdateTriggerEachRow(String tableName, List<String> columnList, List<String> primaryKeys) {

        String filter = genOldNewColumToStore(primaryKeys, "new");

        String updatingStr = "";
        for (String col : columnList) {
            updatingStr = updatingStr + " IF UPDATING (\'" + col + "\') THEN " +
                                        " nd := nd || \', " + col + " > \' || :new." + col + ";" +
                                        " od := od || \', " + col + " > \' || :old." + col + "; END IF; ";
        }
        updatingStr = updatingStr + " nd := SUBSTR(nd, 3); od := SUBSTR(od, 3); ";

        String name = "TRIGGER" + "_UPDATE_" + tableName.toUpperCase() + " ";
        String statement = " AFTER UPDATE ON " + tableName.toUpperCase() + " ";
        String restriction = " ";
        String actionWhen = " FOR EACH ROW DECLARE nd CLOB; od CLOB; ";
        String actionBody = " BEGIN " +
                            updatingStr +
                            " INSERT INTO " + eventLogTableName + " ( " + eventLogColumnNames + " ) " +
                            " VALUES " + " ( " +
                eventLogTableName.toUpperCase() + "_PK_SEQ.nextval, " + " \'" + tableName.toUpperCase() + "\', \'UPDATE\', " +
                filter + ", " + "nd " + ", " + "od " + ", current_timestamp, \'active\'" +
                            " ); " +
                            " END; ";

        writeORInsertSQL(name, statement, restriction, actionWhen, actionBody);
    }

    private void writeORInsertSQL(String name, String statement, String restriction, String actionWhen, String actionBody) {
        String sql = "CREATE OR REPLACE TRIGGER " + name + statement + restriction + actionWhen + actionBody;

        try {
            if(writeTriggerToFileOnly) {
                this.writer.newLine();
                this.writer.write(sql);
                this.writer.newLine();
            } else {
                jdbcTemplate.execute(sql);
            }
        } catch (Exception ex) {
            logger.error("Error Creating Trigger with name " + name, ex);
        }
    }

    private boolean createTableWithSeq(String tableName, String createTableSQL) {

        try {
            logger.info("Preparing to execute create table with name {}", tableName.toUpperCase());
            jdbcTemplate.execute(createTableSQL);
            logger.info("Table created with name {}", tableName.toUpperCase());
        } catch (Exception ex) {
            logger.error("Error creating table with name {}", tableName.toUpperCase());
        }

        try {
            String sqlForSeq = "CREATE SEQUENCE " + tableName.toUpperCase() + "_PK_SEQ START WITH 1 INCREMENT BY 1";
            jdbcTemplate.execute(sqlForSeq);
            logger.info("Sequence created for table {}", tableName.toUpperCase());
        } catch (Exception ex) {
            logger.error("Error creating sequence for table {}", tableName.toUpperCase());
        }
        return true;
    }

    private boolean storeSyncTableInfo(String tableName, String columnWithType, String uniqueColumns) {

        String sql = "INSERT INTO " + syncTableInfo.toUpperCase() + " (ID, TABLE_NAME, COLUMN_LIST, UNIQUE_COLUMNS, CREATE_DATE_TIME, STATUS) " +
                " VALUES (SYNC_TABLE_INFO_PK_SEQ.nextval, ?, ?, ?, current_timestamp, ?)";

        try {
            jdbcTemplate.update(sql, tableName, columnWithType, uniqueColumns, "active");
        } catch (DuplicateKeyException dux) {
            logger.warn("Duplicate key error");
        } catch (Exception ex) {
            logger.error("Error inserting sync-table-info with table-name {}", tableName);
            return false;
        }
        return true;
    }

    private boolean createSyncInfoTable(String tableName) {

        logger.info("Preparing sql for SYNC_TABLE_INFO table");

        String sql = "CREATE TABLE " + tableName.toUpperCase() + " ( " +
                "   ID INTEGER PRIMARY KEY, " +
                "   TABLE_NAME VARCHAR(100) NOT NULL UNIQUE, " +
                "   COLUMN_LIST CLOB NOT NULL, " +
                "   UNIQUE_COLUMNS CLOB NOT NULL, " +
                "   CREATE_DATE_TIME TIMESTAMP NOT NULL, " +
                "   STATUS VARCHAR(20) NOT NULL " +
                " )";

        return createTableWithSeq(tableName.toUpperCase(), sql);    }

    private String genOldNewColumToStore(List<String> dataList, String oldOrNew) {
        String ret = "";
        for( String val : dataList) {
            ret = ret + " || \', " + val + " > \' || " + ":" + oldOrNew + "." + val;
        }
        ret = "\'" + ret.substring(7);

        return ret;
    }

}
