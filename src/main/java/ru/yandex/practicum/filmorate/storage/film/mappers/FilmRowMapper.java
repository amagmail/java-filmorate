package ru.yandex.practicum.filmorate.storage.film.mappers;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class FilmRowMapper implements RowMapper<Film> {

    @Override
    public Film mapRow(ResultSet resultSet, int rowNum) throws SQLException {
        Film film = new Film();
        film.setId(resultSet.getLong("id"));
        film.setName(resultSet.getString("name"));
        film.setDescription(resultSet.getString("description"));
        film.setReleaseDate(resultSet.getDate("release_date").toLocalDate());
        film.setDuration(resultSet.getInt("duration"));

        Set<Long> likes = new HashSet<>();
        long cnt = resultSet.getLong("likes");
        likes.add(cnt);
        film.setLikes(likes);

        Mpa mpa = new Mpa();
        mpa.setId(resultSet.getLong("mpa"));
        mpa.setName(resultSet.getString("mpa_name"));
        mpa.setDescription(resultSet.getString("mpa_description"));
        film.setMpa(mpa);

        String genreIds = resultSet.getString("genre_ids");
        String genreNames = resultSet.getString("genre_names");
        if (genreIds != null && genreNames != null) {
            String[] arrIds = genreIds.split(",");
            String[] arrNames = genreNames.split(",");
            if (arrIds.length == arrNames.length) {
                List<Genre> genres = new ArrayList<>();
                for (int i = 0; i < arrIds.length; i++) {
                    Genre genre = new Genre();
                    Long genreId = Long.valueOf(arrIds[i].trim());
                    String genreName = arrNames[i].trim();
                    genre.setId(genreId);
                    genre.setName(genreName);
                    genres.add(genre);
                }
                film.setGenres(genres);
            }
        }

        String directorIds = resultSet.getString("director_ids");
        String directorNames = resultSet.getString("director_names");
        if (directorIds != null && directorNames != null) {
            String[] arrIds = directorIds.split(",");
            String[] arrNames = directorNames.split(",");
            if (arrIds.length == arrNames.length) {
                Set<Director> directors = new HashSet<>();
                for (int i = 0; i < arrIds.length; i++) {
                    Director director = new Director();
                    Long directorId = Long.valueOf(arrIds[i].trim());
                    String directorName = arrNames[i].trim();
                    director.setId(directorId);
                    director.setName(directorName);
                    directors.add(director);
                }
                film.setDirectors(directors);
            }
        }

        return film;
    }

}
