package com.nomad.app.service;

import com.nomad.app.core.SyncConfigurer;
import com.nomad.app.repository.OracleTriggerImpl;
import com.nomad.app.repository.PostgresDBDAOImpl;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Md Shariful Islam
 */
@Service
public class SyncManager {

    private static final Logger logger = LoggerFactory.getLogger(SyncManager.class);

    @Autowired
    private Environment env;

//    @Autowired
//    @Qualifier("jdbc-03")
//    private JdbcTemplate jdbcTemplate03;

//    @Autowired
//    @Qualifier("sink-jdbc-01")
//    JdbcTemplate sinkJdbcTemplate1;

//    @Autowired
//    @Qualifier("sink-dbcon-01")
//    SinkDBConn sinkDBConn01;

    @Autowired
    private BeanFactory beanFactory;

    @Autowired
    OracleTriggerImpl oracleTrigger;

    @Autowired
    SyncConfigurer syncConfigurer;

    private static Map<String, JdbcTemplate> sourceDBMap = new HashMap<>();
    private static Map<String, JdbcTemplate> sinkDBMap = new HashMap<>();

    private static Map<String, OracleTriggerImpl> oracleTriggerMap = new HashMap<>();
    private static Map<String, PostgresDBDAOImpl> postgresDBDAOMap = new HashMap<>();

//    private Thread[] dataImportThread = new Thread[10];
    private List<Thread> dataImportThreadList = new ArrayList<>();


    @PostConstruct
    private void init(){
        logger.info("sync-manager-init");
        try {
            for (String dbName : env.getRequiredProperty("source.db-list").split("\\s*,\\s*")) {
                sourceDBMap.put(dbName, initializeJdbc(dbName));
            }
            logger.info("generated source db list");

            for (String dbName : env.getRequiredProperty("sink.db-list").split("\\s*,\\s*")) {
                sinkDBMap.put(dbName, initializeJdbc(dbName));
            }
            logger.info("generated sink db list");
        } catch (Exception ex) {
            logger.error("Error at init SyncManager: ", ex);
        }

        logger.info("getting started oracle trigger");
        getSourceDBMap().forEach( (dbName, jdbc) -> {
            logger.info("create trigger for db-name {}", dbName);
            OracleTriggerImpl oracleTrigger = beanFactory.getBean(OracleTriggerImpl.class);
            oracleTrigger.init(dbName, jdbc);
            oracleTrigger.process();
            oracleTriggerMap.put(dbName, oracleTrigger);
        });
        logger.info("oracle trigger generated");

        logger.info("getting started sink init");
        getSinkDBMap().forEach( (dbName, jdbc) -> {
            PostgresDBDAOImpl postgresDBDAO = beanFactory.getBean(PostgresDBDAOImpl.class);
            String sourceDBName = syncConfigurer.getSinkToSourceMap().get(dbName);
            postgresDBDAO.init( sourceDBMap.get(sourceDBName), sinkDBMap.get(dbName), sourceDBName, dbName);
            postgresDBDAOMap.put(dbName, postgresDBDAO);
        });
        logger.info("sink initialized");

        logger.info("started concurrent data import for sink databases");

        concurentImport();
    }

    public void concurentImport() {
        //TODO
        postgresDBDAOMap.forEach( (dbName, postgresDBDAO) -> {
            int threadIndex = 0;

            dataImportThreadList.add(new Thread(() -> {
                while (true) {
                    postgresDBDAO.dataImport();
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        break;
                    }
                }
            }, "data-import-thread-" + threadIndex));
        });
        dataImportThreadList.forEach(Thread::start);
    }

    private JdbcTemplate initializeJdbc(String dbName) {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setDriverClassName(env.getRequiredProperty(dbName + ".driver"));
        dataSource.setJdbcUrl(env.getRequiredProperty(dbName + ".url"));
        dataSource.setUsername(env.getRequiredProperty(dbName + ".user"));
        dataSource.setPassword(env.getRequiredProperty(dbName + ".password"));
        dataSource.setMaximumPoolSize(env.getRequiredProperty("max.poolSize", Integer.class));
        dataSource.setAutoCommit(true);

        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.setFetchSize(env.getRequiredProperty(dbName + ".fetchSize", Integer.class));

        return jdbcTemplate;
    }

    public Map<String, JdbcTemplate> getSourceDBMap() {
        return sourceDBMap;
    }

    public Map<String, JdbcTemplate> getSinkDBMap() {
        return sinkDBMap;
    }
}
