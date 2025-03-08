package ru.yandex.practicum.filmorate.storage.user.mappers;

import org.springframework.jdbc.core.RowMapper;
import ru.yandex.practicum.filmorate.model.Feed;

import java.sql.ResultSet;
import java.sql.SQLException;

public class FeedRowMapper implements RowMapper<Feed> {

    @Override
    public Feed mapRow(ResultSet resultSet, int rowNum) throws SQLException {
        Feed feed = new Feed();
        feed.setEventId(resultSet.getLong("event_id"));
        feed.setTimestamp(resultSet.getLong("timestamp"));
        feed.setUserId(resultSet.getLong("user_id"));
        feed.setEventType(resultSet.getString("event_type"));
        feed.setOperation(resultSet.getString("operation"));
        feed.setEntityId(resultSet.getLong("entity_id"));
        return feed;
    }

}
