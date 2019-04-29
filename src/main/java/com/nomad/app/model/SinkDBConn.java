package com.nomad.app.model;

import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Md Shariful Islam
 */
public class SinkDBConn {
    private JdbcTemplate jdbc;
    private Map<EnumerationList.Proeprties, String> config = new HashMap<>();

    public SinkDBConn(JdbcTemplate jdbcTemplate) {
        this.jdbc = jdbcTemplate;
    }

    public SinkDBConn(DataSource dataSource) {
        this.jdbc = new JdbcTemplate(dataSource);
    }

    public JdbcTemplate getJdbc() {
        return jdbc;
    }

    public SinkDBConn setJdbc(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
        return this;
    }

    public Map<EnumerationList.Proeprties, String> getConfig() {
        return config;
    }

    public SinkDBConn setConfig(Map<EnumerationList.Proeprties, String> config) {
        this.config = config;
        return this;
    }
}
