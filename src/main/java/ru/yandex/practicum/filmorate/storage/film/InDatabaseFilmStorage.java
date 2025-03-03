package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.InternalServerException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.utils.DatabaseUtils;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.*;

@Primary
@Component
public class InDatabaseFilmStorage implements FilmStorage {

    private final JdbcTemplate jdbc;
    private final RowMapper<Film> mapper;

    private static final String BASE_DATA_QUERY = "select films.*, " +
            "mpa.name as mpa_name, " +
            "mpa.description as mpa_description, " +
            "string_agg(genres.id, ', ') as genre_ids, " +
            "string_agg(genres.name, ', ') as genre_names, " +
            "(select count(user_id) from likes where film_id = films.id) as likes " +
            "from films " +
            "left join film_genre fg on fg.film_id = films.id " +
            "left join genres on genres.id = fg.genre_id " +
            "left join mpa on mpa.id = films.mpa";

    private static final String GET_ITEMS = BASE_DATA_QUERY + " group by films.id";
    private static final String GET_ITEM = BASE_DATA_QUERY + " where films.id = ? group by films.id";
    private static final String GET_POPULAR = BASE_DATA_QUERY + " group by films.id order by likes desc limit ?";
    private static final String INSERT_ITEM = "insert into films(name, description, release_date, duration, mpa) values (?, ?, ?, ?, ?)";
    private static final String UPDATE_ITEM = "update films set name = ?, description = ?, release_date = ?, duration = ?, mpa = ? where id = ?";

    private static final String SET_LIKE = "insert into likes(film_id, user_id) values(?, ?)";
    private static final String REMOVE_LIKE = "delete from likes where film_id = ? and user_id = ?";
    private static final String GET_LIKES = "select user_id from likes where film_id = ?";

    private static final String SET_GENRE = "insert into film_genre(film_id, genre_id) values(?, ?)";
    private static final String REMOVE_GENRES = "delete from film_genre where film_id = ?";

    public InDatabaseFilmStorage(JdbcTemplate jdbc, RowMapper<Film> mapper) {
        this.jdbc = jdbc;
        this.mapper = mapper;
    }

    @Override
    public Film create(Film entity) {

        Long mpaId = getValidMpaId(entity.getMpa());
        Set<Long> genreIds = getValidGenreIds(entity.getGenres());

        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(INSERT_ITEM, Statement.RETURN_GENERATED_KEYS);
            ps.setObject(1, entity.getName());
            ps.setObject(2, entity.getDescription());
            ps.setObject(3, entity.getReleaseDate());
            ps.setObject(4, entity.getDuration());
            ps.setObject(5, mpaId);
            return ps;
        }, keyHolder);
        Long id = keyHolder.getKeyAs(Long.class);
        if (id != null) {
            entity.setId(id);
            for (Long genreId : genreIds) {
                jdbc.update(SET_GENRE, id, genreId);
            }
            return entity;
        } else {
            throw new InternalServerException("Не удалось сохранить данные");
        }
    }

    @Override
    public Film update(Film entity) {

        Long mpaId = getValidMpaId(entity.getMpa());
        Set<Long> genreIds = getValidGenreIds(entity.getGenres());

        int rowsUpdated = jdbc.update(UPDATE_ITEM, entity.getName(), entity.getDescription(), entity.getReleaseDate(), entity.getDuration(), mpaId, entity.getId());
        if (rowsUpdated > 0) {
            jdbc.update(REMOVE_GENRES, entity.getId());
            for (Long genreId : genreIds) {
                jdbc.update(SET_GENRE, entity.getId(), genreId);
            }
            return entity;
        } else {
            throw new InternalServerException("Не удалось обновить данные");
        }
    }

    @Override
    public Collection<Film> getItems() {
        return jdbc.query(GET_ITEMS, mapper);
    }

    @Override
    public Film getItem(Long filmId) {
        return jdbc.queryForObject(GET_ITEM, mapper, filmId);
    }

    @Override
    public Set<Long> setLike(Long filmId, Long userId) {
        Set<Long> likes = getLikes(filmId);
        if (!likes.contains(userId)) {
            int rowsUpdated = jdbc.update(SET_LIKE, filmId, userId);
            if (rowsUpdated > 0) {
                likes.add(userId);
            }
        }
        return likes;
    }

    @Override
    public Set<Long> removeLike(Long filmId, Long userId) {
        jdbc.update(REMOVE_LIKE, filmId, userId);
        return getLikes(filmId);
    }

    @Override
    public Collection<Film> getPopular(int count) {
        return jdbc.query(GET_POPULAR, mapper, count);
    }

    public Set<Long> getLikes(Long filmId) {
        List<Long> checkVals = DatabaseUtils.getExistRows(jdbc, "films", List.of(filmId));
        if (checkVals.isEmpty()) {
            throw new NotFoundException("Не удалось найти фильмы по идентификаторам");
        }
        List<Long> userIds = jdbc.queryForList(GET_LIKES, Long.class, filmId);
        return new HashSet<>(userIds);
    }

    private Long getValidMpaId(Mpa mpa) {
        Long mpaId = (mpa != null) ? mpa.getId() : null;
        if (mpaId != null) {
            List<Long> checkVals = DatabaseUtils.getExistRows(jdbc, "mpa", List.of(mpaId));
            if (checkVals.isEmpty()) {
                throw new NotFoundException("Не удалось найти рейтинги по идентификаторам");
            }
        }
        return mpaId;
    }

    private Set<Long> getValidGenreIds(Set<Genre> genres) {
        Set<Long> genreIds = new HashSet<>();
        for (Genre genre : genres) {
            genreIds.add(genre.getId());
        }
        List<Long> checkVals = DatabaseUtils.getExistRows(jdbc, "genres", new ArrayList<>(genreIds));
        if (checkVals.size() != genreIds.size()) {
            throw new NotFoundException("Не удалось найти жанры по идентификаторам");
        }
        return genreIds;
    }

}
