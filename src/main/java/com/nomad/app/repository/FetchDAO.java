package com.nomad.app.repository;

import com.nomad.app.model.EventLog;

import java.util.List;

/**
 * @author Md Shariful Islam
 */
public interface FetchDAO {
    List<EventLog> getEvent(Integer id, int size);
}
