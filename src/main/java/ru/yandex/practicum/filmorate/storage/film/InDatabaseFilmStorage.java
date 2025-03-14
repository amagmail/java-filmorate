package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.InternalServerException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.film.mappers.FilmListRowMapper;
import ru.yandex.practicum.filmorate.storage.film.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.utils.DatabaseUtils;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.*;

@Slf4j
@Primary
@Component
public class InDatabaseFilmStorage implements FilmStorage {

    private final JdbcTemplate jdbc;
    private final RowMapper<Film> mapper;
    private final RowMapper<Collection<Film>> listMapper;

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

    private static final String REMOVE_FILM = "delete from films where id = ?";
    private static final String CLEAR_LIKES = "delete from likes where film_id = ?";
    private static final String CLEAR_FILM_GENRE = "delete from film_genre where film_id = ? ";
    private static final String GET_COMMON_FILMS_QUERY = "select f.*, " +
            "g.id as genre_id, g.name as genre_name, " +
            "m.name as mpa_name, m.id as mpa_id, " +
            "(select count(film_id) from likes as l where l.film_id = f.id) as likes " +
            "from films as f " +
            "left join film_genre as fg on f.id = fg.film_id " +
            "left join genres as g on fg.genre_id = g.id " +
            "left join likes as l1 on f.id = l1.film_id " +
            "left join likes as l2 on f.id = l2.film_id " +
            "left join mpa as m on f.mpa = m.id " +
            "where l1.user_id = ? and l2.user_id = ? " +
            "order by likes desc";

    private static final String FIND_SIMILAR_USERS =
            "select l2.user_id as similar_user_id, count(*) as common_likes " +
                    "from likes l1 " +
                    "join likes l2 on l1.film_id = l2.film_id " +
                    "where l1.user_id = ? and l2.user_id != ? " +
                    "group by l2.user_id " +
                    "order by common_likes desc " +
                    "limit 10";
    private static final String FIND_FILMS_LIKED_BY_USER_BUT_NOT_TARGET =
            "select l.film_id " +
                    "from likes l " +
                    "where l.user_id = ? and l.film_id not in ( " +
                    "    select film_id " +
                    "    from likes " +
                    "    where user_id = ? " +
                    ")";

    private static final String POPULAR_GROUP = " group by bs.id order by bs.likes desc limit ?";
    private static final String POPULAR_WHERE_GENRE = "exists(SELECT 1 FROM film_genre fg WHERE fg.film_id = bs.id AND fg.genre_id = ?)";
    private static final String POPULAR_WHERE_YEAR = "extract(YEAR FROM bs.RELEASE_DATE) = ?";

    public InDatabaseFilmStorage(JdbcTemplate jdbc, RowMapper<Film> mapper) {
        this.jdbc = jdbc;
        this.mapper = new FilmRowMapper();
        this.listMapper = new FilmListRowMapper();
    }

    @Override
    public Film create(Film entity) {

        Long mpaId = getValidMpaId(entity.getMpa());
        List<Long> genreIds = getValidGenreIds(entity.getGenres());
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
        List<Long> genreIds = getValidGenreIds(entity.getGenres());
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
        Collection<Film> films = jdbc.query(GET_ITEM, mapper, filmId);
        if (films.isEmpty()) {
            throw new NotFoundException("Не удалось фильм по идентификатору");
        }
        return films.iterator().next();
    }

    @Override
    public Set<Long> setLike(Long filmId, Long userId) {
        Set<Long> likes = getLikes(filmId);
        int rowsUpdated = jdbc.update(SET_LIKE, filmId, userId);
        if (rowsUpdated > 0) {
            likes.add(userId);
            DatabaseUtils.addDataToFeed(jdbc, userId, "LIKE", "ADD", filmId);
        }
        return likes;
    }

    @Override
    public Set<Long> removeLike(Long filmId, Long userId) {
        jdbc.update(REMOVE_LIKE, filmId, userId);
        DatabaseUtils.addDataToFeed(jdbc, userId, "LIKE", "REMOVE", filmId);
        return getLikes(filmId);
    }

    @Override
    public Collection<Film> getPopular(int count, Long genreId, Integer year) {

        Collection<Film> films;
        String query = BASE_DATA_QUERY;

        String whereGenre = "";
        if (genreId != null) {
            whereGenre = POPULAR_WHERE_GENRE;
        }

        String whereYear = "";
        if (year != null) {
            whereYear = POPULAR_WHERE_YEAR;
        }

        if (!whereGenre.isEmpty() && !whereYear.isEmpty()) {
            query += " where " + whereGenre + " and " + whereYear + POPULAR_GROUP;
            films = jdbc.query(query, mapper, genreId, year, count);
        } else if (!whereGenre.isEmpty()) {
            query += " where " + whereGenre + POPULAR_GROUP;
            films = jdbc.query(query, mapper, genreId, count);
        } else if (!whereYear.isEmpty()) {
            query += " where " + whereYear + POPULAR_GROUP;
            films = jdbc.query(query, mapper, year, count);
        } else {
            query += POPULAR_GROUP;
            films = jdbc.query(query, mapper, count);
        }
        return films;
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

    private List<Long> getValidGenreIds(List<Genre> genres) {
        List<Long> genreIds = new ArrayList<>();
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

    @Override
    public Film removeFilm(Long filmId) {
        clearLikesForFilm(filmId);
        clearFilmGenre(filmId);
        log.info("Будем удалять фильм по ID: {}", filmId);
        if (filmId == null) {
            throw new ValidationException("ID фильма пуст. Введите значение и повторите попытку.");
        }
        Film film = getItem(filmId);
        jdbc.update(REMOVE_FILM, filmId);
        log.info("Удален фильм({}) по ID: {}", film, filmId);
        return film;
    }

    @Override
    public Collection<Film> getCommonFilms(Long userId, Long friendId) {
        if (userId == null || friendId == null) {
            throw new ValidationException("ID пользователя пуст. Введите значение и повторите попытку.");
        }

        try {
            return jdbc.query(GET_COMMON_FILMS_QUERY, listMapper, userId, friendId).getFirst();
        } catch (NoSuchElementException e) {
            return Collections.emptyList();
        } catch (Exception e) {
            throw new RuntimeException("Произошла ошибка", e);
        }
    }

    @Override
    public void clearLikesForFilm(Long filmId) {
        jdbc.update(CLEAR_LIKES, filmId);
    }

    private void clearFilmGenre(Long filmId) {
        jdbc.update(CLEAR_FILM_GENRE, filmId);
    }

    @Override
    public List<Long> findSimilarUsers(Long userId) {
        return jdbc.query(FIND_SIMILAR_USERS, (rs, rowNum) -> rs.getLong("similar_user_id"), userId, userId);
    }

    @Override
    public List<Long> findFilmsLikedByUserButNotTarget(Long similarUserId, Long targetUserId) {
        return jdbc.queryForList(FIND_FILMS_LIKED_BY_USER_BUT_NOT_TARGET, Long.class, similarUserId, targetUserId);
    }

    @Override
    public Collection<Film> getFilmsByIds(List<Long> filmIds) {
        if (filmIds.isEmpty()) {
            return Collections.emptyList();
        }

        String sql = BASE_DATA_QUERY + " WHERE bs.id IN (" + String.join(",", Collections.nCopies(filmIds.size(), "?")) + ")";
        return jdbc.query(sql, mapper, filmIds.toArray());
    }
}
