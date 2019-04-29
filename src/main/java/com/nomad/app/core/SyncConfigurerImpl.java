package com.nomad.app.core;

import com.nomad.app.model.EnumerationList;
import org.javatuples.Triplet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Md Shariful Islam
 */
@Component
public class SyncConfigurerImpl implements SyncConfigurer {

    private static final Logger logger = LoggerFactory.getLogger(SyncConfigurerImpl.class);

    @Autowired
    Environment environment;

    private List<String> dbList;
    private Map<String, List<String>> tableList = new HashMap<>();
    private Map<Triplet<String, String, String>, List<String>> columnList = new HashMap<>();


    @PostConstruct
    public void init(){
        for (String db : getSyncDBList(true)) {
            getSyncTableList(db, true);
            storeSyncColumnList(db);
        }
    }

    @Override
    public List<String> getSyncDBList(boolean isRefresh) {
        if(dbList != null && isRefresh == false) return dbList;
        logger.info("Getting sync-db-list");
        dbList = Arrays.asList(environment.getRequiredProperty("db3.sink-db-list").split("\\s*,\\s*"));
        return dbList;
    }

    @Override
    public List<String> getSyncTableList(String dbName, boolean isRefresh) {
        if(tableList.containsKey(dbName) && isRefresh == false) return tableList.get(dbName);
        logger.info("Getting sync-table-list for db {}", dbName);
        tableList.put(dbName, Arrays.asList(environment.getRequiredProperty(dbName + "." + "sync-table-list").split("\\s*\\[(.*?)\\]\\s*,*\\s*")));
        return tableList.get(dbName);
    }

    // need to store first
    // return null means process all columns
    @Override
    public List<String> getSyncColumnList(String dbName, String tableName, EnumerationList.Operator op, boolean isRefresh) {
        if(isRefresh) {
            storeSyncColumnList(dbName);
        }
        return columnList.get(new Triplet<>(dbName,tableName,op.toString()));
    }

    @Override
    public Map<Triplet<String, String, String>, List<String>> getAllColumnList() {
        for (String db : getSyncDBList(true) ) {
            storeSyncColumnList(db);
        }
        return columnList;
    }

    @Override
    public void storeSyncColumnList(String dbName) {
        logger.info("Getting sync-column-list for db {}", dbName);
        for (String tmp : environment.getRequiredProperty(dbName + "." + "sync-table-list").split("],*\\s*")) {
            int p = tmp.indexOf("[") + 1;
            if (p > tmp.length()) continue;
            String tblName = tmp.substring(0, p - 1);
            tmp = tmp.substring(p);

            for (String x : tmp.split("\\),*\\s*")) {
                if(x.isEmpty()) continue;
                int px = x.indexOf("(") + 1;
                if (px > x.length()) continue;
                columnList.put(new Triplet<>(dbName, tblName, x.substring(0, px - 1)), Arrays.asList(x.substring(px).split("\\s*,\\s*")));
            }
        }
    }
}
