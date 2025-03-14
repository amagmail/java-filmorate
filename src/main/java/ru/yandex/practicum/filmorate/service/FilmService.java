package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.director.DirectorStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.*;

@RequiredArgsConstructor
@Service
public class FilmService {

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final DirectorStorage directorStorage;

    public Set<Long> setLike(Long filmId, Long userId) {
        if (userStorage.getItem(userId) == null) {
            throw new NotFoundException("Пользователь с идентификатором " + userId + " не существует");
        }
        return filmStorage.setLike(filmId, userId);
    }

    public Set<Long> removeLike(Long filmId, Long userId) {
        if (userStorage.getItem(userId) == null) {
            throw new NotFoundException("Пользователь с идентификатором " + userId + " не существует");
        }
        return filmStorage.removeLike(filmId, userId);
    }

    public Collection<Film> getPopular(int count, Long genreId, Integer year) {
        return filmStorage.getPopular(count, genreId, year);
    }

    public Film create(Film film) {
        List<Genre> genres = film.getGenres();
        if (genres != null && !genres.isEmpty()) {
            Set<Genre> set = new HashSet<>(genres);
            genres.clear();
            genres.addAll(set);
            genres.sort(Comparator.comparing(Genre::getId));
            film.setGenres(genres);
        }
        return filmStorage.create(film);
    }

    public Film update(Film film) {
        if (film.getId() == null) {
            throw new ValidationException("Поле id содержит невалидное значение");
        }
        if (filmStorage.getItem(film.getId()) == null) {
            throw new NotFoundException("Фильм с идентификатором " + film.getId() + " не существует");
        }
        List<Genre> genres = film.getGenres();
        if (genres != null && !genres.isEmpty()) {
            Set<Genre> set = new HashSet<>(genres);
            genres.clear();
            genres.addAll(set);
            genres.sort(Comparator.comparing(Genre::getId));
            film.setGenres(genres);
        }
        return filmStorage.update(film);
    }

    public Collection<Film> getItems() {
        return filmStorage.getItems();
    }

    public Film getItem(Long filmId) {
        return filmStorage.getItem(filmId);
    }

    public Collection<Film> getDirectorFilms(Long directorId, String sortBy) {
        if (directorStorage.getItem(directorId) == null) {
            throw new NotFoundException("Режиссера с идентификатором " + directorId + " не существует");
        }
        sortBy = sortBy.toLowerCase();
        sortBy = sortBy.replace("year", "release_date");
        return filmStorage.getDirectorFilms(directorId, sortBy);
    }

    public Collection<Film> getFilmsSearch(String searchVal, String searchFields) {
        if (searchVal != null) {
            searchVal = searchVal.toLowerCase();
        }
        if (searchFields != null) {
            searchFields = searchFields.toLowerCase();
        }
        return filmStorage.getFilmsSearch(searchVal, searchFields);
    }

    public Film removeFilm(Long filmId) {
        return filmStorage.removeFilm(filmId);
    }

    public List<Film> getCommonFilms(Long userId, Long friendId) {
        return filmStorage.getCommonFilms(userId, friendId);
    }
}

