package ru.yandex.practicum.filmorate.storage.mpa;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.utils.DatabaseUtils;

import java.util.Collection;
import java.util.List;

@Component
public class InDatabaseMpaStorage implements MpaStorage {

    private final JdbcTemplate jdbc;
    private final RowMapper<Mpa> mapper;

    private static final String GET_ITEMS = "select * from mpa";
    private static final String GET_ITEM = "select * from mpa where id = ?";

    public InDatabaseMpaStorage(JdbcTemplate jdbc, RowMapper<Mpa> mapper) {
        this.jdbc = jdbc;
        this.mapper = mapper;
    }

    @Override
    public Mpa create(Mpa entity) {
        return null;
    }

    @Override
    public Mpa update(Mpa entity) {
        return null;
    }

    @Override
    public Collection<Mpa> getItems() {
        return jdbc.query(GET_ITEMS, mapper);
    }

    @Override
    public Mpa getItem(Long id) {
        List<Long> checkVals = DatabaseUtils.getExistRows(jdbc, "mpa", List.of(id));
        if (checkVals.isEmpty()) {
            throw new NotFoundException("Не удалось найти рейтинги по идентификаторам");
        }
        return jdbc.queryForObject(GET_ITEM, mapper, id);
    }

}
