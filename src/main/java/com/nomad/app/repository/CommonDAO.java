package com.nomad.app.repository;

import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Map;

/**
 * @author Md Shariful Islam
 */
public interface CommonDAO {
    Map<String, Object> getImportedKeys(JdbcTemplate jdbcTampleName, String catalog, String schema, String table);
    Map<String, Object> getPrimaryKeys(JdbcTemplate jdbcTampleName, String catalog, String schema, String table);
}
