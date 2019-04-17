package com.nomad.app.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * @author Md Shariful Islam
 */
@Repository
public class OracleDAOImpl implements OracleDAO {

    private static final Logger logger = LoggerFactory.getLogger(OracleDAOImpl.class);

    @Autowired
    @Qualifier("jdbc-03")
    JdbcTemplate jdbcTemplate03;

    public boolean createSimpleTrigger() {
        String sql =    " CREATE OR REPLACE TRIGGER trigger_test AFTER INSERT OR DELETE ON BIOMETRIC " +
                        " FOR EACH ROW DECLARE column_name VARCHAR(100); BEGIN INSERT INTO " + 
                        " SUPPLIER(SUPPLIER_ID, SUPPLIER_NAME, CONTACT_NAME) VALUES (biometric_pk_seq.nextval ,'tt', 'ss'); END;";

        logger.info("Getting started to creat trigger...");

        try {
            jdbcTemplate03.execute(sql);
        } catch(Exception ex) {
            logger.error("Error creating trigger :: ", ex);
            return false;
        }
        logger.info("Trigger Created.");

        return true;
    }
}