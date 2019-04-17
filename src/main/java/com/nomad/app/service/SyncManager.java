package com.nomad.app.service;

import com.nomad.app.repository.CommonDAO;
import com.nomad.app.repository.OracleDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

/**
 * @author Md Shariful Islam
 */
@Service
public class SyncManager {

    @Autowired
    private Environment env;

    @Autowired
    @Qualifier("jdbc-01")
    private JdbcTemplate jdbcTemplate01;

    @Autowired
    @Qualifier("jdbc-02")
    private JdbcTemplate jdbcTemplate02;

    @Autowired
    CommonDAO commonDAO;

    @Autowired
    OracleDAO oracleDAO;

    @Value("${db1.schema}")
    private String db01Schema;
    @Value("${db1.test-table}")
    private String db01TestTable;

    @Value("${db2.schema}")
    private String db02Schema;
    @Value("${db2.test-table}")
    private String db02TestTable;


    @PostConstruct
    private void init(){
        //TODO
//        String db01Catalog = env.getRequiredProperty("db1.catalog").equalsIgnoreCase("null")? null:env.getRequiredProperty("db1.catalog");
//        String db02Catalog = env.getRequiredProperty("db2.catalog").equalsIgnoreCase("null")? null:env.getRequiredProperty("db2.catalog");
//
//        Map<String, Object> importKeyList = commonDAO.getImportedKeys( jdbcTemplate01, db01Catalog,db01Schema,db01TestTable);
//        Map<String, Object> primaryKeyInfo = commonDAO.getPrimaryKeys( jdbcTemplate02, db02Catalog,db01Schema,db01TestTable);

        try {
            oracleDAO.createSimpleTrigger();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

}
