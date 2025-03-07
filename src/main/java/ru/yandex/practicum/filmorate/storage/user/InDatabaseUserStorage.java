package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.InternalServerException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.utils.DatabaseUtils;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Primary
@Component
public class InDatabaseUserStorage implements UserStorage {

    private final JdbcTemplate jdbc;
    private final RowMapper<User> mapper;

    private static final String GET_ITEMS = "select *, (select count(friend_id) from friendship where user_id = id) as friends from users";
    private static final String GET_ITEM = "select *, (select count(friend_id) from friendship where user_id = id) as friends from users where id = ?";
    private static final String INSERT_QUERY = "insert into users(name, email, login, birthday) values (?, ?, ?, ?)";
    private static final String UPDATE_QUERY = "update users set name = ?, email = ?, login = ?, birthday = ? where id = ?";

    private static final String SET_FRIEND = "insert into friendship(user_id, friend_id) values(?, ?)";
    private static final String REMOVE_FRIEND = "delete from friendship where user_id = ? and friend_id = ?";
    private static final String GET_FRIENDS = "select friend_id from friendship where user_id = ?";

    private static final String GET_USER_FRIENDS = "select t2.*, (select count(friend_id) from friendship where user_id = t2.id) as friends " +
            "from friendship t1 " +
            "inner join users t2 on t2.id = t1.friend_id " +
            "where t1.user_id = ?";

    private static final String GET_MUTUAL_FRIENDS = "select *, (select count(friend_id) from friendship where user_id = id) as friends " +
            "from users " +
            "where id in (select friend_id from friendship where user_id = ?) " +
            "and id in (select friend_id from friendship where user_id = ?)";

    private static final String ACTUALIZE_FRIENDSHIPS_TRUE = "update friendship set accepted = true " +
            "where ((user_id = ? and friend_id = ?) or (user_id = ? and friend_id = ?)) " +
            "and exists(select user_id, friend_id from friendship where user_id = ? and friend_id = ?) " +
            "and exists(select user_id, friend_id from friendship where user_id = ? and friend_id = ?)";

    private static final String ACTUALIZE_FRIENDSHIPS_FALSE = "update friendship set accepted = false " +
            "where user_id = ? and friend_id = ?";
    private static final String REMOVE_USER = "DELETE FROM users WHERE id = ?";

    public InDatabaseUserStorage(JdbcTemplate jdbc, RowMapper<User> mapper) {
        this.jdbc = jdbc;
        this.mapper = mapper;
    }

    @Override
    public User create(User entity) {
        if (entity.getName() == null || entity.getName().isBlank()) {
            entity.setName(entity.getLogin());
        }
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(INSERT_QUERY, Statement.RETURN_GENERATED_KEYS);
            ps.setObject(1, entity.getName());
            ps.setObject(2, entity.getLogin());
            ps.setObject(3, entity.getEmail());
            ps.setObject(4, entity.getBirthday());
            return ps;
        }, keyHolder);
        Long id = keyHolder.getKeyAs(Long.class);
        if (id != null) {
            entity.setId(id);
            return entity;
        } else {
            throw new InternalServerException("Не удалось сохранить данные");
        }
    }

    @Override
    public User update(User entity) {
        int rowsUpdated = jdbc.update(UPDATE_QUERY, entity.getName(), entity.getLogin(), entity.getEmail(), entity.getBirthday(), entity.getId());
        if (rowsUpdated > 0) {
            return entity;
        } else {
            throw new NotFoundException("Пользователя с идентификатором " + entity.getId() + " не существует");
        }
    }

    @Override
    public Collection<User> getItems() {
        return jdbc.query(GET_ITEMS, mapper);
    }

    @Override
    public User getItem(Long userId) {
        List<User> users = jdbc.query(GET_ITEM, mapper, userId);
        if (users.isEmpty()) {
            throw new NotFoundException("Не удалось найти пользователя по идентификатору");
        }
        return users.getFirst();
    }

    @Override
    public Set<Long> setFriend(Long userId, Long friendId) {
        List<Long> checkVals = DatabaseUtils.getExistRows(jdbc, "users", List.of(userId, friendId));
        if (checkVals.size() != 2) {
            throw new NotFoundException("Не удалось найти пользователей по идентификаторам");
        }
        Set<Long> friends = getFriends(userId);
        if (!friends.contains(friendId)) {
            int rowsUpdated = jdbc.update(SET_FRIEND, userId, friendId);
            if (rowsUpdated > 0) {
                friends.add(friendId);
                jdbc.update(ACTUALIZE_FRIENDSHIPS_TRUE, userId, friendId, friendId, userId, userId, friendId, friendId, userId);
            }
        }
        return friends;
    }

    @Override
    public Set<Long> removeFriend(Long userId, Long friendId) {
        List<Long> checkVals = DatabaseUtils.getExistRows(jdbc, "users", List.of(userId, friendId));
        if (checkVals.size() != 2) {
            throw new NotFoundException("Не удалось найти пользователей по идентификаторам");
        }
        int rowsUpdated = jdbc.update(REMOVE_FRIEND, userId, friendId);
        if (rowsUpdated > 0) {
            jdbc.update(ACTUALIZE_FRIENDSHIPS_FALSE, friendId, userId);
        }
        return getFriends(userId);
    }

    @Override
    public Collection<User> getUserFriends(Long userId) {
        List<Long> checkVals = DatabaseUtils.getExistRows(jdbc, "users", List.of(userId));
        if (checkVals.isEmpty()) {
            throw new NotFoundException("Не удалось найти пользователей по идентификаторам");
        }
        return jdbc.query(GET_USER_FRIENDS, mapper, userId);
    }

    @Override
    public Collection<User> getMutualFriends(Long userId, Long otherId) {
        return jdbc.query(GET_MUTUAL_FRIENDS, mapper, userId, otherId);
    }

    public Set<Long> getFriends(Long userId) {
        List<Long> userIds = jdbc.queryForList(GET_FRIENDS, Long.class, userId);
        return new HashSet<>(userIds);
    }

    @Override
    public User removeUser(Long userId) {
        log.info("Будем удалять пользователя по ID: {}", userId);
        if (userId == null) {
            throw new ValidationException("ID пользователя пуст. Введите значение и повторите попытку.");
        }
        User user = getItem(userId);
        jdbc.update(REMOVE_USER, userId);
        log.info("Удален пользователь({}) по ID: {}", user, userId);
        return user;
    }
}
