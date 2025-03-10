package ru.yandex.practicum.filmorate.storage.film.mappers;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Component
public class FilmListRowMapper implements RowMapper<List<Film>> {

    @Override
    public List<Film> mapRow(ResultSet resultSet, int rowNum) throws SQLException {
        Map<Long, Film> filmsMap = new HashMap<>();
        do {
            Long filmId = resultSet.getLong("id");
            Film film = filmsMap.get(filmId);
            if (film == null) {
                film = new Film();
                film.setId(resultSet.getLong("id"));
                film.setName(resultSet.getString("name"));
                film.setDescription(resultSet.getString("description"));
                film.setReleaseDate(resultSet.getDate("release_date").toLocalDate());
                film.setDuration(resultSet.getInt("duration"));
                Set<Long> likes = new HashSet<>();
                long cnt = resultSet.getLong("likes");
                likes.add(cnt);
                film.setLikes(likes);

                if (resultSet.getLong("MPA_ID") != 0) {
                    Mpa mpa = new Mpa();
                    mpa.setId(resultSet.getLong("MPA_ID"));
                    mpa.setName(resultSet.getString("MPA_NAME"));
                    film.setMpa(mpa);
                }
                filmsMap.put(film.getId(), film);
            }

            if (resultSet.getLong("GENRE_ID") != 0) {
                Genre genre = new Genre();
                genre.setId(resultSet.getLong("GENRE_ID"));
                genre.setName(resultSet.getString("GENRE_NAME"));

                if (!film.getGenres().contains(genre)) {
                    film.getGenres().add(genre);
                }
            }
        } while (resultSet.next());

        return filmsMap.values().stream().toList();
    }
}