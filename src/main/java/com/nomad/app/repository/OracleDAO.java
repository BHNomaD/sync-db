package com.nomad.app.repository;

import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Map;

/**
 * @author Md Shariful Islam
 */
public interface OracleDAO {
	boolean createSimpleTrigger();
	boolean executeQuery(String sql);
}
