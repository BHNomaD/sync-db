package com.nomad.app.repository;

import com.nomad.app.model.EventLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * @author Md Shariful Islam
 */
@Repository
public class FetchDAOImpl implements FetchDAO {

    private static final Logger logger = LoggerFactory.getLogger(FetchDAOImpl.class);


    @Autowired
    @Qualifier("jdbc-03")
    JdbcTemplate jdbcTemplate03;


    @Override
    public List<EventLog> getEvent(Integer id, int size) {

        String sql =" SELECT ID, ORIGINAL_TABLE_NAME, OPERATION, FILTER, NEW_DATA, OLD_DATA, CREATE_DATE_TIME, STATUS " +
                    " FROM EVENT_LOG WHERE ID > ? ORDER BY ID FETCH NEXT ? ROW ONLY";

        return jdbcTemplate03.query(sql, (rs, rowNum) -> new EventLog()
            .setId(rs.getInt("ID"))
            .setOriginalTableName(rs.getString("ORIGINAL_TABLE_NAME"))
            .setOperation(rs.getString("OPERATION"))
            .setFilter(rs.getString("FILTER"))
            .setNewData(rs.getBytes("NEW_DATA"))
            .setOldData(rs.getBytes("OLD_DATA"))
            .setCreateDateTime(rs.getTimestamp("CREATE_DATE_TIME"))
            .setStatus(rs.getString("STATUS"))
        , id, size);
    }
}
