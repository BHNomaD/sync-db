package com.nomad.app.repository;

/**
 * @author Md Shariful Islam
 */
public interface OracleDAO {
	boolean createSimpleTrigger();
	boolean executeQuery(String sql);
}
