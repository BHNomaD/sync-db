package com.nomad.app.core;

import com.nomad.app.model.EnumerationList;
import org.javatuples.Triplet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Md Shariful Islam
 */
@Component
public class SyncConfigurerImpl implements SyncConfigurer {

    private static final Logger logger = LoggerFactory.getLogger(SyncConfigurerImpl.class);

    @Autowired
    Environment environment;

    private List<String> sinkDbList = new ArrayList<>();
    private Set<String> sinkDbSet = new HashSet<>();
    private Map<String, List<String>> tableList = new HashMap<>();
    private Map<Triplet<String, String, String>, List<String>> columnList = new HashMap<>();
    Map<String, Map<String, String>> propertiesMap = new HashMap<>();


    @PostConstruct
    public void init(){
        for (String db : getSyncDBSet(true)) {
            getSyncTableList(db, true);
            storeSyncColumnList(db);
            loadPropertiesMap(db);
        }
    }

    @Override
    public Map<String, String> getPropertiesMap(String db, boolean isRefresh) {
        if(propertiesMap.containsKey(db) == false || isRefresh) {
            loadPropertiesMap(db);
        }
        return propertiesMap.get(db);
    }

    private boolean loadPropertiesMap(String db) {
        Map<String, String> map = new HashMap<>();
        for (EnumerationList.Proeprties p : EnumerationList.Proeprties.values()) {
            map.put(p.toString(), environment.getProperty( db + "." + p.getValue()));
        }
        propertiesMap.put(db, map);
        return true;
    }

    @Override
    public List<String> getSyncDBList(boolean isRefresh) {
        if(sinkDbList.size() > 0 && isRefresh == false) return sinkDbList;
        logger.info("Getting sync-db-list");
        for ( String sourceDb : environment.getRequiredProperty("source.db-list").split("\\s*,\\s*")) {
            sinkDbList.addAll(Arrays.asList(environment.getRequiredProperty(sourceDb + ".sink-db-list").split("\\s*,\\s*")));
        }
        return sinkDbList;
    }

    @Override
    public Set<String> getSyncDBSet(boolean isRefresh) {
        if(sinkDbSet.size() > 0 && isRefresh == false) return sinkDbSet;
        logger.info("Getting sync-db-list");
        for ( String sourceDb : environment.getRequiredProperty("source.db-list").split("\\s*,\\s*")) {
            sinkDbSet.addAll(Arrays.stream(environment.getRequiredProperty(sourceDb + ".sink-db-list").split("\\s*,\\s*")).collect(Collectors.toSet()));
//            In JDK 9+
//            sinkDbSet.addAll(Set.of(environment.getRequiredProperty(sourceDb + ".sink-db-list").split("\\s*,\\s*")));
        }
        return sinkDbSet;
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
        for (String db : getSyncDBSet(false) ) {
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
