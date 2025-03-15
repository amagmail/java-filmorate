package ru.yandex.practicum.filmorate.storage.director;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.InternalServerException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.utils.DatabaseUtils;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Collection;
import java.util.List;

@Component
public class InDatabaseDirectorStorage implements DirectorStorage {

    private final JdbcTemplate jdbc;
    private final RowMapper<Director> mapper;

    private static final String GET_ITEMS = "select * from directors";
    private static final String GET_ITEM = "select * from directors where id = ?";
    private static final String INSERT_QUERY = "insert into directors(name) values (?)";
    private static final String UPDATE_QUERY = "update directors set name = ? where id = ?";
    private static final String REMOVE_QUERY = "delete from directors where id = ?";
    private static final String REMOVE_QUERY_IN_FILM_DIRECTOR = "delete from film_director where director_id = ?";

    public InDatabaseDirectorStorage(JdbcTemplate jdbc, RowMapper<Director> mapper) {
        this.jdbc = jdbc;
        this.mapper = mapper;
    }

    @Override
    public Director create(Director entity) {
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(INSERT_QUERY, Statement.RETURN_GENERATED_KEYS);
            ps.setObject(1, entity.getName());
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
    public Director update(Director entity) {
        int rowsUpdated = jdbc.update(UPDATE_QUERY, entity.getName(), entity.getId());
        if (rowsUpdated > 0) {
            return entity;
        } else {
            throw new NotFoundException("Режиссера с идентификатором " + entity.getId() + " не существует");
        }
    }

    @Override
    public Collection<Director> getItems() {
        return jdbc.query(GET_ITEMS, mapper);
    }

    @Override
    public Director getItem(Long id) {
        List<Long> checkVals = DatabaseUtils.getExistRows(jdbc, "directors", List.of(id));
        if (checkVals.isEmpty()) {
            throw new NotFoundException("Не удалось найти режиссера по идентификатору");
        }
        return jdbc.queryForObject(GET_ITEM, mapper, id);
    }

    @Override
    public Boolean remove(Long directorId) {
        List<Long> checkVals = DatabaseUtils.getExistRows(jdbc, "directors", List.of(directorId));
        if (checkVals.isEmpty()) {
            throw new NotFoundException("Не удалось найти режиссера по идентификатору");
        }
        jdbc.update(REMOVE_QUERY_IN_FILM_DIRECTOR, directorId);
        int rowsUpdated = jdbc.update(REMOVE_QUERY, directorId);
        return rowsUpdated > 0;
    }

}
