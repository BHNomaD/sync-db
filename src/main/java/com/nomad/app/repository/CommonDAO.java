package com.nomad.app.repository;

import groovy.lang.Tuple2;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;

/**
 * @author Md Shariful Islam
 */
public interface CommonDAO {
    List<Map<String, String>> getImportedKeys(JdbcTemplate jdbcTampleName, String catalog, String schema, String table);
    List<Map<String, String>> getPrimaryKeys(JdbcTemplate jdbcTampleName, String catalog, String schema, String table);
    List<Tuple2<String, String>> getColumnInfo(JdbcTemplate jdbcTampleName, String table);
}
