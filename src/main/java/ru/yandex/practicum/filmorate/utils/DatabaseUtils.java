package ru.yandex.practicum.filmorate.utils;

import org.springframework.jdbc.core.JdbcTemplate;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

public class DatabaseUtils {

    public static List<Long> getExistRows(JdbcTemplate jdbc, String tableName, List<Long> ids) {
        String query = "select id from " + tableName + " where id in (";
        query += String.join(",", Collections.nCopies(ids.size(), "?"));
        query += ")";
        return jdbc.queryForList(query, Long.class, ids.toArray());
    }

    public static void addDataToFeed(JdbcTemplate jdbc, Long userId, String eventType, String operation, Long entityId) {
        String query = "insert into feed (timestamp, user_id, event_type, operation, entity_id) values (?, ?, ?, ?, ?)";
        jdbc.update(query, Instant.now().getEpochSecond() * 1000, userId, eventType, operation, entityId);
    }

}
