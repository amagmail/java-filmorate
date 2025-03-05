package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.InternalServerException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
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

    private static final String BASE_DATA_QUERY = "select bs.*, " +
            "string_agg(directors.id, ', ') as director_ids, " +
            "string_agg(directors.name, ', ') as director_names " +
            "from ( " +
                "select films.*, " +
                "mpa.name as mpa_name, " +
                "mpa.description as mpa_description, " +
                "string_agg(genres.id, ', ') as genre_ids, " +
                "string_agg(genres.name, ', ') as genre_names, " +
                "(select count(user_id) from likes where film_id = films.id) as likes " +
                "from films " +
                "left join film_genre fg on fg.film_id = films.id " +
                "left join genres on genres.id = fg.genre_id " +
                "left join mpa on mpa.id = films.mpa " +
                "group by films.id " +
            ") bs " +
            "left join film_director fd on fd.film_id = bs.id " +
            "left join directors on directors.id = fd.director_id";

    private static final String GET_ITEMS = BASE_DATA_QUERY + " group by bs.id";
    private static final String GET_ITEM = BASE_DATA_QUERY + " where bs.id = ? group by bs.id";
    private static final String GET_POPULAR = BASE_DATA_QUERY + " group by bs.id order by bs.likes desc limit ?";
    private static final String GET_DIRECTOR_FILMS = BASE_DATA_QUERY + " where directors.id = ? group by bs.id";
    private static final String INSERT_ITEM = "insert into films(name, description, release_date, duration, mpa) values (?, ?, ?, ?, ?)";
    private static final String UPDATE_ITEM = "update films set name = ?, description = ?, release_date = ?, duration = ?, mpa = ? where id = ?";

    private static final String SET_LIKE = "insert into likes(film_id, user_id) values(?, ?)";
    private static final String REMOVE_LIKE = "delete from likes where film_id = ? and user_id = ?";
    private static final String GET_LIKES = "select user_id from likes where film_id = ?";

    private static final String SET_GENRE = "insert into film_genre(film_id, genre_id) values(?, ?)";
    private static final String REMOVE_GENRES = "delete from film_genre where film_id = ?";

    private static final String SET_DIRECTOR = "insert into film_director(film_id, director_id) values(?, ?)";
    private static final String REMOVE_DIRECTORS = "delete from film_director where film_id = ?";

    public InDatabaseFilmStorage(JdbcTemplate jdbc, RowMapper<Film> mapper) {
        this.jdbc = jdbc;
        this.mapper = mapper;
    }

    @Override
    public Film create(Film entity) {

        Long mpaId = getValidMpaId(entity.getMpa());
        Set<Long> genreIds = getValidGenreIds(entity.getGenres());
        Set<Long> directorIds = getValidDirectorIds(entity.getDirectors());

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
            for (Long directorId : directorIds) {
                jdbc.update(SET_DIRECTOR, id, directorId);
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
        Set<Long> directorIds = getValidDirectorIds(entity.getDirectors());

        int rowsUpdated = jdbc.update(UPDATE_ITEM, entity.getName(), entity.getDescription(), entity.getReleaseDate(), entity.getDuration(), mpaId, entity.getId());
        if (rowsUpdated > 0) {
            jdbc.update(REMOVE_GENRES, entity.getId());
            for (Long genreId : genreIds) {
                jdbc.update(SET_GENRE, entity.getId(), genreId);
            }
            jdbc.update(REMOVE_DIRECTORS, entity.getId());
            for (Long directorId : directorIds) {
                jdbc.update(SET_DIRECTOR, entity.getId(), directorId);
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

    @Override
    public Collection<Film> getDirectorFilms(Long directorId, String field) {
        if (field.equals("likes")) {
            field += " desc";
        }
        if (field.equals("release_date")) {
            field += " asc";
        }
        return jdbc.query(GET_DIRECTOR_FILMS + " order by " + field, mapper, directorId);
    }

    @Override
    public Collection<Film> getFilmsSearch(String searchVal, String searchFields) {
        String query = "select src.* from (" + GET_ITEMS + ") src ";
        String whereStr = "";
        if (!searchVal.isEmpty() && !searchFields.isEmpty()) {
            String condition;
            List<String> fields = List.of(searchFields.split(","));
            for (String field : fields) {
                switch (field) {
                    case "title" -> condition = String.format("lower(src.name) like '%%%s%%'", searchVal);
                    case "director" -> condition = String.format("lower(src.director_names) like '%%%s%%'", searchVal);
                    default -> condition = String.format("lower(src." + field + ") like '%%%s%%'", searchVal);
                }
                if (whereStr.isEmpty()) {
                    whereStr += "where " + condition + " ";
                } else {
                    whereStr += "or " + condition + " ";
                }
            }
        }
        query += whereStr + "order by src.likes desc";
        return jdbc.query(query, mapper);
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

    private Set<Long> getValidDirectorIds(Set<Director> directors) {
        Set<Long> directorIds = new HashSet<>();
        for (Director director : directors) {
            directorIds.add(director.getId());
        }
        List<Long> checkVals = DatabaseUtils.getExistRows(jdbc, "genres", new ArrayList<>(directorIds));
        return directorIds;
    }

}
