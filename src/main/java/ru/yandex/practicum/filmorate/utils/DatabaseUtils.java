package ru.yandex.practicum.filmorate.utils;

import org.springframework.jdbc.core.JdbcTemplate;
import java.util.Collections;
import java.util.List;

public class DatabaseUtils {

    public static List<Long> getExistRows(JdbcTemplate jdbc, String tableName, List<Long> ids) {
        String query = "select id from " + tableName + " where id in (";
        query += String.join(",", Collections.nCopies(ids.size(), "?"));
        query += ")";
        return jdbc.queryForList(query, Long.class, ids.toArray());
    }

}
