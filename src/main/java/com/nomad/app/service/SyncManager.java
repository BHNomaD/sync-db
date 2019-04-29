package com.nomad.app.service;

import com.nomad.app.Application;
import com.nomad.app.core.SyncConfigurer;
import com.nomad.app.model.EnumerationList;
import com.nomad.app.model.EventLog;
import com.nomad.app.model.SinkDBConn;
import com.nomad.app.repository.FetchDAO;
import com.nomad.app.repository.OracleTriggerImpl;
import com.nomad.app.repository.PostgresDBDAOImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
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
    private BeanFactory beanFactory;

    @Autowired
    OracleTriggerImpl oracleTrigger;

    @Autowired
    FetchDAO fetchDAO;

    @Autowired
    @Qualifier("sink-jdbc-01")
    JdbcTemplate sinkJdbcTemplate1;

    @Autowired
    @Qualifier("sink-dbcon-01")
    SinkDBConn sinkDBConn01;

    @Autowired
    SyncConfigurer syncConfigurer;
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
            List<EventLog> eventLogList = fetchDAO.getEvent(1, Integer.parseInt(env.getRequiredProperty("sink-db1.sync-size")));
            System.out.println(eventLogList);

            PostgresDBDAOImpl postgresDBDAO01 = beanFactory.getBean(PostgresDBDAOImpl.class, sinkDBConn01);
            postgresDBDAO01.init();
            //TODO auto-determine sink-db-name


            //TODO
            String db = "sink-db1", tbl = "TB1", opr = EnumerationList.Operator.UPDATE.toString();
            List<String> cList = syncConfigurer.getSyncColumnList(db, tbl, EnumerationList.Operator.UPDATE, false);
            cList.forEach( x -> System.out.println(db + "::" + tbl + "::" + opr + ":-->:" + x ));

            postgresDBDAO01.createSyncTable("TB1");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

}
