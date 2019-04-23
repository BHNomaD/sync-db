package com.nomad.app.service;

import com.nomad.app.repository.CommonDAO;
import com.nomad.app.repository.OracleDAO;
import com.nomad.app.repository.OracleDAOImpl;
import com.nomad.app.repository.OracleTriggerImpl;
import groovy.lang.Tuple2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Md Shariful Islam
 */
@Service
public class SyncManager {

//    @Autowired
//    @Qualifier("jdbc-01")
//    private JdbcTemplate jdbcTemplate01;
//
//    @Autowired
//    @Qualifier("jdbc-02")
//    private JdbcTemplate jdbcTemplate02;
//
//    @Autowired
//    CommonDAO commonDAO;
//
//    @Autowired
//    OracleDAO oracleDAO;

    @Autowired
    OracleTriggerImpl oracleTrigger;

//    @Value("${db1.schema}")
//    private String db01Schema;
//    @Value("${db1.test-table}")
//    private String db01TestTable;
//
//    @Value("${db2.schema}")
//    private String db02Schema;
//    @Value("${db2.test-table}")
//    private String db02TestTable;


    @PostConstruct
    private void init(){
        try {
            //Create triggers for sync-table-list
            oracleTrigger.process();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

}
