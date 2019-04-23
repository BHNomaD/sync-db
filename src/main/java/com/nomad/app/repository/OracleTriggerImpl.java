package com.nomad.app.repository;

import com.nomad.app.model.EnumerationList;
import groovy.lang.Tuple2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author Md Shariful Islam
 */
@Service
public class OracleTriggerImpl implements TriggerTemplate {

    private static final Logger logger = LoggerFactory.getLogger(OracleTriggerImpl.class);

    @Autowired
    private Environment env;

    @Autowired
    @Qualifier("jdbc-03")
    JdbcTemplate jdbcTemplate03;

    @Autowired
    CommonDAO commonDAO;

    @Value("${db3.schema}")
    private String schema;

    @Value("${db3.catalog}")
    private String catalog;

    @Value("${db3.event-log-table-name}")
    private String eventLongTableName;

    @Value("${db3.sync-table-list}")
    private String syncTableList;

    String eventLogColumnNames = "ID,ORIGINAL_TABLE_NAME,OPERATION,FILTER,NEW_DATA,OLD_DATA,CREATE_DATE_TIME,STATUS";

    @Override
    public void process(){
        //TODO - METHOD DONE
//        CREATE IF NOT EXISTS EVENT TABLE
        createEventTable();

        //TODO - METHOD DONE
//        GET ALL TABLE NAME TO BE SYNC
//        syncTableList

        //TODO - METHOD DONE
//        GET COLUMN NAMES OF THE TABLES
//        GET PRIMARY KEYS OF THE TABLES
//        CREATE INSERT, UPDATE, DELETE TRIGGER TO THE TABLES

        for ( String tableName : syncTableList.split("\\s*,\\s*")) {
            List<String> columnList = new ArrayList<>();
            getColumnInfo(tableName).forEach( col -> columnList.add(col.getFirst()));
            List<String> primaryKeys = getPrimaryKeys(tableName);
            createInsertTriggerEachRow(tableName, columnList, primaryKeys);
            createDeleteTriggerEachRow(tableName, columnList, primaryKeys);
            createUpdateTriggerEachRow(tableName, columnList, primaryKeys);
        }
    }

    @Override
    public boolean createEventTable() {

        logger.info("Preparing sql for create table with name {}", eventLongTableName);

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

        String sqlForSeq = "CREATE SEQUENCE " + eventLongTableName.toUpperCase() + "_PK_SEQ START WITH 1 INCREMENT BY 1";

        logger.info("Preparing to execute create table with name {}", eventLongTableName);

        try {
            jdbcTemplate03.execute(sql);
            logger.info("Table created with name {}", eventLongTableName);
        } catch (Exception ex) {
            logger.error("Error creating table with name {}", eventLongTableName);
            return false;
        }

        try {
            jdbcTemplate03.execute(sqlForSeq);
            logger.info("Sequence created for table {}", eventLongTableName);
        } catch (Exception ex) {
            logger.error("Error creating sequence for table {}", eventLongTableName);
        }

        return true;
    }

    @Override
    public List<String> getSyncTableList(String dbName) {
        String[] syncTableList = env.getRequiredProperty(dbName + ".sync-table-list").split(",");
        return Arrays.asList(syncTableList);
    }

    @Override
    public List<Tuple2<String, String>> getColumnInfo(String table) {
        List<Tuple2<String, String>> columnInfos = commonDAO.getColumnInfo(jdbcTemplate03, table);
        return columnInfos;
    }

    @Override
    public List<String> getPrimaryKeys(String table) {
        List<Map<String, String>> primaryKeys = commonDAO.getPrimaryKeys(jdbcTemplate03, catalog, schema, table);
        List<String> pkIds = new ArrayList<>();
        primaryKeys.forEach( x -> pkIds.add(x.get(EnumerationList.PrimaryKeys.COLUMN_NAME.toString())));
        return pkIds;
    }

    private String genOldNewColumToStore(List<String> dataList, String oldOrNew) {
        String ret = "";
        for( String val : dataList) {
            ret = ret + " || \', " + val + " = \' || " + ":" + oldOrNew + "." + val;
        }
        ret = "\'" + ret.substring(7);

        return ret;
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
                            " INSERT INTO " + eventLongTableName + " ( " + eventLogColumnNames + " ) " +
                            " VALUES " + " ( " +
                eventLongTableName.toUpperCase() + "_PK_SEQ.nextval, " + " \'" + tableName.toUpperCase() + "\', \'INSERT\', " +
                filter + ", " + newValues + ", NULL, current_timestamp, \'active\'" +
                            " ); " +
                            " END; ";

        String sql = "CREATE OR REPLACE TRIGGER " + name + statement + restriction + actionWhen + actionBody;

        try {
            jdbcTemplate03.execute(sql);
        } catch (Exception ex) {
            logger.error("Error Creating Trigger with name " + name, ex);
        }
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
                            " INSERT INTO " + eventLongTableName + " ( " + eventLogColumnNames + " ) " +
                            " VALUES " + " ( " +
                eventLongTableName.toUpperCase() + "_PK_SEQ.nextval, " + " \'" + tableName.toUpperCase() + "\', \'DELETE\', " +
                filter + ", NULL, " + oldValues + ", current_timestamp, \'active\'" +
                            " ); " +
                            " END; ";

        String sql = "CREATE OR REPLACE TRIGGER " + name + statement + restriction + actionWhen + actionBody;

        try {
            jdbcTemplate03.execute(sql);
        } catch (Exception ex) {
            logger.error("Error Creating Trigger with name " + name, ex);
        }
    }

    @Override
    public void createUpdateTriggerEachRow(String tableName, List<String> columnList, List<String> primaryKeys) {

        String filter = genOldNewColumToStore(primaryKeys, "new");

        String updatingStr = "";
        for (String col : columnList) {
            updatingStr = updatingStr + " IF UPDATING (\'" + col + "\') THEN " +
                                        " nd := nd || \', " + col + " = \' || :new." + col + "; " +
                                        " od := od || \', " + col + " = \' || :old." + col + "; END IF; ";
        }
        updatingStr = updatingStr + " nd := SUBSTR(nd, 3); od := SUBSTR(od, 3); ";

        String name = "TRIGGER" + "_UPDATE_" + tableName.toUpperCase() + " ";
        String statement = " AFTER UPDATE ON " + tableName.toUpperCase() + " ";
        String restriction = " ";
        String actionWhen = " FOR EACH ROW DECLARE nd CLOB; od CLOB; ";
        String actionBody = " BEGIN " +
                            updatingStr +
                            " INSERT INTO " + eventLongTableName + " ( " + eventLogColumnNames + " ) " +
                            " VALUES " + " ( " +
                eventLongTableName.toUpperCase() + "_PK_SEQ.nextval, " + " \'" + tableName.toUpperCase() + "\', \'UPDATE\', " +
                filter + ", " + "nd " + ", " + "od " + ", current_timestamp, \'active\'" +
                            " ); " +
                            " END; ";

        String sql = "CREATE OR REPLACE TRIGGER " + name + statement + restriction + actionWhen + actionBody;

        try {
            jdbcTemplate03.execute(sql);
        } catch (Exception ex) {
            logger.error("Error Creating Trigger with name " + name, ex);
        }
    }
}
