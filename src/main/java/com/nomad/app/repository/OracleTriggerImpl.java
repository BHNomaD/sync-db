package com.nomad.app.repository;

import groovy.lang.Tuple2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
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

    @Override
    public boolean createEventTable(String tableName) {

        logger.info("Preparing sql for create table with name {}", tableName);

        String sql = " CREATE TABLE " + tableName + " (" +
                    "    id INTEGER PRIMARY KEY," +
                    "    original_table_name VARCHAR(100) NOT NULL," +
                    "    operation VARCHAR(10) NOT NULL," +
                    "    filter VARCHAR(200) NOT NULL," +
                    "    new_data VARCHAR(200)," +
                    "    old_data VARCHAR(200)," +
                    "    create_date_time TIMESTAMP NOT NULL," +
                    "    status VARCHAR(50) NOT NULL" +
                    ")";

        logger.info("Preparing to execute create table with name {}", tableName);

        try {
            jdbcTemplate03.execute(sql);
        } catch (Exception ex) {
            logger.error("Error creating table with name {}", tableName);
            return false;
        }
        logger.info("Table created with name {}", tableName);

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
    public List<String> getPrimaryKeys(String catalog, String schema, String table) {
        List<Map<String, String>> primaryKeys = commonDAO.getPrimaryKeys(jdbcTemplate03, catalog, schema, table);
        List<String> pkIds = new ArrayList<>();
        primaryKeys.forEach( x -> pkIds.add(x.get("PK_NAME")));
        return pkIds;
    }

    @Override
    public List<Map<String, Object>> getMetadataList(List<String> tableNameList) {
        return null;
    }

    @Override
    public void createTrigger(String tableName) {

    }

    @Override
    public void createTriggerList(List<String> tableNameList) {
    }
}
