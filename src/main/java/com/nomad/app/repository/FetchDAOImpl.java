package com.nomad.app.repository;

import com.nomad.app.model.EventLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author Md Shariful Islam
 */
@Repository
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class FetchDAOImpl implements FetchDAO {

    private static final Logger logger = LoggerFactory.getLogger(FetchDAOImpl.class);

    private JdbcTemplate sourceJdbc;

    FetchDAOImpl(JdbcTemplate sourceJdbc) {
        this.sourceJdbc = sourceJdbc;
    }

    @Override
    public List<EventLog> getEvent(int id, int size) {
        try {
            logger.info("Getting events id-from {} with size {}", id, size);

            String sql =" SELECT ID, ORIGINAL_TABLE_NAME, OPERATION, FILTER, NEW_DATA, OLD_DATA, CREATE_DATE_TIME, STATUS " +
                        " FROM EVENT_LOG WHERE ID > ? ORDER BY ID FETCH NEXT ? ROW ONLY";

            return sourceJdbc.query(sql, (rs, rowNum) -> new EventLog()
                .setId(rs.getInt("ID"))
                .setOriginalTableName(rs.getString("ORIGINAL_TABLE_NAME"))
                .setOperation(rs.getString("OPERATION"))
                .setFilter(rs.getString("FILTER"))
                .setNewData(rs.getBytes("NEW_DATA"))
                .setOldData(rs.getBytes("OLD_DATA"))
                .setCreateDateTime(rs.getTimestamp("CREATE_DATE_TIME"))
                .setStatus(rs.getString("STATUS"))
            , id, size);
        } catch (Exception ex) {
            logger.error("Error getting metadata: ", ex);
            return null;
        }
    }
}
