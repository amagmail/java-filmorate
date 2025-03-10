package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.Storage;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface FilmStorage extends Storage<Film> {

    Set<Long> setLike(Long filmId, Long userId);

    Set<Long> removeLike(Long filmId, Long userId);

    Collection<Film> getPopular(int count, Long genreId, Integer year);

    Collection<Film> getDirectorFilms(Long directorId, String sortBy);

    Collection<Film> getFilmsSearch(String searchVal, String searchFields);

    Film removeFilm(Long filmId);

    void clearLikesForFilm(Long filmId);

    List<Long> findSimilarUsers(Long userId);

    List<Long> findFilmsLikedByUserButNotTarget(Long similarUserId, Long targetUserId);

    Collection<Film> getFilmsByIds(List<Long> filmIds);
}