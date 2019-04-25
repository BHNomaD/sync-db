package com.nomad.app.service;

import com.nomad.app.Application;
import com.nomad.app.model.EventLog;
import com.nomad.app.repository.FetchDAO;
import com.nomad.app.repository.OracleTriggerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * @author Md Shariful Islam
 */
@Service
public class SyncManager {

    private static final Logger logger = LoggerFactory.getLogger(SyncManager.class);

    @Autowired
    private Environment env;

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

    @Autowired
    FetchDAO fetchDAO;

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
            List<EventLog> eventLogList = fetchDAO.getEvent(1, Integer.parseInt(env.getRequiredProperty("db4.sync-size")));
            System.out.println(eventLogList);

        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

}
