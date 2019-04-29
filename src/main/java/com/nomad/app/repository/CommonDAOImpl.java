package com.nomad.app.repository;

import com.nomad.app.model.EnumerationList;
import com.nomad.app.model.TableInfo;
import org.javatuples.Triplet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Md Shariful Islam
 */
@Repository
public class CommonDAOImpl implements CommonDAO {

    private static final Logger logger = LoggerFactory.getLogger(CommonDAOImpl.class);


    @Override
    public List<Map<String, String>> getImportedKeys(JdbcTemplate jdbcTemplate,String catalog, String schema, String table) {

        List<Map<String, String>> importedKeys = new ArrayList<>();
        ResultSet rs = null;

        try {
            // catalog, schema and table-name is case sensitive
            rs = jdbcTemplate.getDataSource().getConnection().getMetaData().getImportedKeys(catalog.toUpperCase(), schema.toUpperCase(), table.toUpperCase());
        } catch (SQLException ex) {
            logger.error("Error getting ImportedKeys for {}-{}-{} :: ", catalog, schema, table, ex);
        }

        try {
            while (rs.next()) {
                Map<String, String> map = new HashMap<>();
                map.put(EnumerationList.ImportedKeys.PKTABLE_CAT.toString(), rs.getString(EnumerationList.ImportedKeys.PKTABLE_CAT.toString()));
                map.put(EnumerationList.ImportedKeys.PKTABLE_SCHEM.toString(), rs.getString(EnumerationList.ImportedKeys.PKTABLE_SCHEM.toString()));
                map.put(EnumerationList.ImportedKeys.PKTABLE_NAME.toString(), rs.getString(EnumerationList.ImportedKeys.PKTABLE_NAME.toString()));
                map.put(EnumerationList.ImportedKeys.PKCOLUMN_NAME.toString(), rs.getString(EnumerationList.ImportedKeys.PKCOLUMN_NAME.toString()));
                map.put(EnumerationList.ImportedKeys.FKCOLUMN_NAME.toString(), rs.getString(EnumerationList.ImportedKeys.FKCOLUMN_NAME.toString()));
                map.put(EnumerationList.ImportedKeys.UPDATE_RULE.toString(), rs.getString(EnumerationList.ImportedKeys.UPDATE_RULE.toString()));
                map.put(EnumerationList.ImportedKeys.DELETE_RULE.toString(), rs.getString(EnumerationList.ImportedKeys.DELETE_RULE.toString()));
                importedKeys.add(map);
            }
        } catch (Exception ex) {
            logger.error("Error populating ImportedKeys for {}-{}-{} :: ", catalog, schema, table, ex);
        }
        logger.info("ImportedKeys for {}-{}-{} completed ", catalog, schema, table);

        return importedKeys;
    }

    @Override
    public List<Map<String, String>> getPrimaryKeys(JdbcTemplate jdbcTemplate, String catalog, String schema, String table) {

        List<Map<String, String>> primaryKeys = new ArrayList<>();
        ResultSet rs = null;

        try {
            // catalog, schema and table-name is case sensitive
            rs = jdbcTemplate.getDataSource().getConnection().getMetaData().getPrimaryKeys(catalog.toUpperCase(), schema.toUpperCase(), table.toUpperCase());
        } catch (SQLException e) {
            logger.error("Error in getPrimaryKeys() for originJdbcTemplate ", e);
        }

        try {
            while (rs.next()) {
                Map<String, String> map = new HashMap<>();
                map.put(EnumerationList.PrimaryKeys.TABLE_CAT.toString(), rs.getString(EnumerationList.PrimaryKeys.TABLE_CAT.toString()));
                map.put(EnumerationList.PrimaryKeys.TABLE_SCHEM.toString(), rs.getString(EnumerationList.PrimaryKeys.TABLE_SCHEM.toString()));
                map.put(EnumerationList.PrimaryKeys.TABLE_NAME.toString(), rs.getString(EnumerationList.PrimaryKeys.TABLE_NAME.toString()));
                map.put(EnumerationList.PrimaryKeys.COLUMN_NAME.toString(), rs.getString(EnumerationList.PrimaryKeys.COLUMN_NAME.toString()));
                map.put(EnumerationList.PrimaryKeys.KEY_SEQ.toString(), rs.getString(EnumerationList.PrimaryKeys.KEY_SEQ.toString()));
                map.put(EnumerationList.PrimaryKeys.PK_NAME.toString(), rs.getString(EnumerationList.PrimaryKeys.PK_NAME.toString()));
                primaryKeys.add(map);
            }
        } catch (Exception ex) {
            logger.error("Error populating map ", ex);
        }

        return primaryKeys;
    }

    @Override
    public List<Triplet<String, String, Integer>> getColumnInfo(JdbcTemplate jdbcTample, String table) {
        String sql = "SELECT COLUMN_NAME, DATA_TYPE, DATA_LENGTH FROM USER_TAB_COLUMNS WHERE TABLE_NAME = ?";
        return jdbcTample.query(sql, (rs, i) -> new Triplet<>(
                        rs.getString("COLUMN_NAME"),
                        rs.getString("DATA_TYPE"),
                        rs.getInt("DATA_LENGTH")), table);
    }

    @Override
    public TableInfo getTableInfo(JdbcTemplate jdbcTemplate, String tableName) {
        logger.info("Preparing table-info of {}", tableName);
        String sql = "SELECT TABLE_NAME, COLUMN_LIST, UNIQUE_COLUMNS FROM SYNC_TABLE_INFO WHERE UPPER(TABLE_NAME) = UPPER(?) AND STATUS = 'active'";
        return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> new TableInfo()
                .setTableName(rs.getString("TABLE_NAME"))
                .setColumnList(rs.getString("COLUMN_LIST"))
                .setUniqueColumns(rs.getString("UNIQUE_COLUMNS")), tableName);
    }

}
