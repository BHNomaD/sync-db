package com.nomad.app.repository;

import com.nomad.app.model.EnumerationList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Md Shariful Islam
 */
@Repository
public class CommonDAOImpl implements CommonDAO {

    private static final Logger logger = LoggerFactory.getLogger(CommonDAOImpl.class);

//    @Autowired
//    @Qualifier("jdbc-01")
//    private JdbcTemplate jdbcTemplate01;
//
//    @Autowired
//    @Qualifier("jdbc-02")
//    private JdbcTemplate jdbcTemplate02;


    @Override
    public Map<String, Object> getImportedKeys(JdbcTemplate jdbcTemplate,String catalog, String schema, String table) {

        Map<String, Object> map = new HashMap<>();
        ResultSet rs = null;

        try {
//            rs = jdbcTemplate.getDataSource().getConnection().getMetaData().getImportedKeys(null,"app1","r4");
            rs = jdbcTemplate.getDataSource().getConnection().getMetaData().getImportedKeys(catalog, schema, table);
        } catch (SQLException e) {
            logger.error("Error in getImportedKeys() for originJdbcTemplate ", e);
        }

        try {
            while (rs.next()) {
                map.put(EnumerationList.ImportedKeys.PKTABLE_CAT.toString(), rs.getString(EnumerationList.ImportedKeys.PKTABLE_CAT.toString()));
                map.put(EnumerationList.ImportedKeys.PKTABLE_SCHEM.toString(), rs.getString(EnumerationList.ImportedKeys.PKTABLE_SCHEM.toString()));
                map.put(EnumerationList.ImportedKeys.PKTABLE_NAME.toString(), rs.getString(EnumerationList.ImportedKeys.PKTABLE_NAME.toString()));
                map.put(EnumerationList.ImportedKeys.PKCOLUMN_NAME.toString(), rs.getString(EnumerationList.ImportedKeys.PKCOLUMN_NAME.toString()));
                map.put(EnumerationList.ImportedKeys.FKCOLUMN_NAME.toString(), rs.getString(EnumerationList.ImportedKeys.FKCOLUMN_NAME.toString()));
                map.put(EnumerationList.ImportedKeys.UPDATE_RULE.toString(), rs.getString(EnumerationList.ImportedKeys.UPDATE_RULE.toString()));
                map.put(EnumerationList.ImportedKeys.DELETE_RULE.toString(), rs.getString(EnumerationList.ImportedKeys.DELETE_RULE.toString()));
            }
        } catch (Exception ex) {
            logger.error("Error populating map ", ex);
        }

        return map;
    }

    @Override
    public Map<String, Object> getPrimaryKeys(JdbcTemplate jdbcTemplate, String catalog, String schema, String table) {

        Map<String, Object> map = new HashMap<>();
        ResultSet rs = null;

        try {
            rs = jdbcTemplate.getDataSource().getConnection().getMetaData().getPrimaryKeys(catalog, schema, table);
        } catch (SQLException e) {
            logger.error("Error in getPrimaryKeys() for originJdbcTemplate ", e);
        }

        try {
            while (rs.next()) {
                map.put(EnumerationList.PrimaryKeys.TABLE_CAT.toString(), rs.getString(EnumerationList.PrimaryKeys.TABLE_CAT.toString()));
                map.put(EnumerationList.PrimaryKeys.TABLE_SCHEM.toString(), rs.getString(EnumerationList.PrimaryKeys.TABLE_SCHEM.toString()));
                map.put(EnumerationList.PrimaryKeys.TABLE_NAME.toString(), rs.getString(EnumerationList.PrimaryKeys.TABLE_NAME.toString()));
                map.put(EnumerationList.PrimaryKeys.COLUMN_NAME.toString(), rs.getString(EnumerationList.PrimaryKeys.COLUMN_NAME.toString()));
                map.put(EnumerationList.PrimaryKeys.KEY_SEQ.toString(), rs.getString(EnumerationList.PrimaryKeys.KEY_SEQ.toString()));
                map.put(EnumerationList.PrimaryKeys.PK_NAME.toString(), rs.getString(EnumerationList.PrimaryKeys.PK_NAME.toString()));
            }
        } catch (Exception ex) {
            logger.error("Error populating map ", ex);
        }

        return map;
    }
}
