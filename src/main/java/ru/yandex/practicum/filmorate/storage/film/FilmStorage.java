package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.Storage;

import java.util.Collection;
import java.util.Set;

public interface FilmStorage extends Storage<Film> {

    Set<Long> setLike(Long filmId, Long userId);

    Set<Long> removeLike(Long filmId, Long userId);

    Collection<Film> getPopular(int count);

    Collection<Film> getDirectorFilms(Long directorId, String sortBy);

    Collection<Film> getFilmsSearch(String searchVal, String searchFields);

}