package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;
import java.util.Set;

@RequiredArgsConstructor
@Service
public class FilmService {

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

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
        return filmStorage.create(film);
    }

    public Film update(Film film) {
        if (film.getId() == null) {
            throw new ValidationException("Поле id содержит невалидное значение");
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
        return filmStorage.getDirectorFilms(directorId, sortBy);
    }

    public Collection<Film> getFilmsSearch(String searchVal, String searchFields) {
        return filmStorage.getFilmsSearch(searchVal, searchFields);
    }

}
