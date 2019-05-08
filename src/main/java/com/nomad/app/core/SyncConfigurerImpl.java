package com.nomad.app.core;

import com.nomad.app.model.EnumerationList;
import com.nomad.app.model.TableInfo;
import org.javatuples.Pair;
import org.javatuples.Triplet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;

/**
 * @author Md Shariful Islam
 */
@Component
public class SyncConfigurerImpl implements SyncConfigurer {

    private static final Logger logger = LoggerFactory.getLogger(SyncConfigurerImpl.class);

    @Autowired
    Environment environment;

    private Set<String> sourceDBSet = new HashSet<>();
    private Set<String> sinkDBSet = new HashSet<>();
    private Map<String, List<String>> sourceToSinkMap = new HashMap<>();
    private Map<String, String> sinkToSourceMap = new HashMap<>();
    private Map<String, List<String>> tableList = new HashMap<>();
    private Map<Pair<String, String>, TableInfo> tableInfoMap = new HashMap<>();
    private Map<Triplet<String, String, String>, List<String>> columnList = new HashMap<>();
    Map<String, Map<String, String>> propertiesMap = new HashMap<>();


    @PostConstruct
    public void init(){
        for (String db : getSinkDBSet(true)) {
            getSyncTableList(db, true);
            storeSyncColumnList(db);
            loadPropertiesMap(db);
        }
        loadSourceSinkMap();
    }

    @Override
    public Set<String> getSourceDBSet(boolean isRefresh) {
        if(sourceDBSet.size() > 0 && isRefresh == false) return sourceDBSet;
        logger.info("Getting source-db-list");
        sourceDBSet.addAll(Set.of(environment.getRequiredProperty("source.db-list").split("\\s*,\\s*")));
        return sourceDBSet;
    }

    @Override
    public Set<String> getSinkDBSet(boolean isRefresh) {
        if(sinkDBSet.size() > 0 && isRefresh == false) return sinkDBSet;
        logger.info("Getting sink-db-list");
        for ( String sourceDb : getSourceDBSet(true)) {
            sinkDBSet.addAll(Set.of(environment.getRequiredProperty(sourceDb + ".sink-db-list").split("\\s*,\\s*")));
        }
        return sinkDBSet;
    }

    @Override
    public Map<String, List<String>> getSourceToSinkMap() {
        return sourceToSinkMap;
    }

    @Override
    public Map<String, String> getSinkToSourceMap() {
        return sinkToSourceMap;
    }

    @Override
    public TableInfo getTableInfo(String dbName, String tableName) {
        if(tableInfoMap.containsKey(new Pair<>(dbName, tableName))) {
            return tableInfoMap.get(new Pair<>(dbName, tableName));
        } else return null;
    }

    @Override
    public void addTableInfo(String dbName, String tableName, TableInfo tableInfo) {
        tableInfoMap.put(new Pair<>(dbName, tableName), tableInfo);
    }

    @Override
    public Map<String, String> getPropertiesMap(String db, boolean isRefresh) {
        if(propertiesMap.containsKey(db) == false || isRefresh) {
            loadPropertiesMap(db);
        }
        return propertiesMap.get(db);
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
        if(columnList.containsKey(new Triplet<>(dbName,tableName,op.toString())) == false) {
            columnList.put(new Triplet<>(dbName,tableName,op.toString()),
                    new ArrayList(getTableInfo(sinkToSourceMap.get(dbName), tableName).getColumnMap().keySet()));
        }
        return columnList.get(new Triplet<>(dbName,tableName,op.toString()));
    }

    @Override
    public Map<Triplet<String, String, String>, List<String>> getAllColumnList() {
        for (String db : getSinkDBSet(false) ) {
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

    private boolean loadSourceSinkMap() {
        for (String dbName : getSinkDBSet(false)) {
            String sourceDB = environment.getProperty(dbName + "." + EnumerationList.Proeprties.SOURCE_DB.getValue());
            sinkToSourceMap.put(dbName, sourceDB);
            if(sourceToSinkMap.containsKey(sourceDB) == false) {
                sourceToSinkMap.put(sourceDB, new ArrayList<>());
            }
            sourceToSinkMap.get(sourceDB).add(dbName);
        }
        return true;
    }

    private boolean loadPropertiesMap(String db) {
        Map<String, String> map = new HashMap<>();
        for (EnumerationList.Proeprties p : EnumerationList.Proeprties.values()) {
            map.put(p.toString(), environment.getProperty( db + "." + p.getValue()));
        }
        propertiesMap.put(db, map);
        return true;
    }
}
