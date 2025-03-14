package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import ru.yandex.practicum.filmorate.model.Film;
import org.springframework.web.bind.annotation.*;

import java.util.*;

import ru.yandex.practicum.filmorate.service.FilmService;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/films")
public class FilmController {

    private final FilmService filmService;

    @PutMapping("/{filmId}/like/{userId}")
    public Set<Long> setLike(@PathVariable("filmId") Long filmId, @PathVariable("userId") Long userId) {
        log.info("Set like: {} - filmId, {} - userId", filmId, userId);
        return filmService.setLike(filmId, userId);
    }

    @DeleteMapping("/{filmId}/like/{userId}")
    public Set<Long> removeLike(@PathVariable("filmId") Long filmId, @PathVariable("userId") Long userId) {
        log.info("Remove like: {} - filmId, {} - userId", filmId, userId);
        return filmService.removeLike(filmId, userId);
    }

    @GetMapping("/popular")
    public Collection<Film> getPopular(@RequestParam(value = "count", defaultValue = "10") int count,
                                       @RequestParam(value = "genreId", required = false) Long genreId,
                                       @RequestParam(value = "year", required = false) Integer year) {
        log.info("Get popular films: {} - count, {} - genreId, {} - year", count, genreId, year);
        return filmService.getPopular(count, genreId, year);
    }

    @GetMapping("/{filmId}")
    public Film getItem(@PathVariable("filmId") Long filmId) {
        log.info("Get film by ID: {}", filmId);
        return filmService.getItem(filmId);
    }

    @GetMapping
    public Collection<Film> getItems() {
        return filmService.getItems();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Film create(@Valid @RequestBody Film film) {
        log.info("Create film: {}", film.getName());
        return filmService.create(film);
    }

    @PutMapping
    public Film update(@Valid @RequestBody Film newFilm) {
        log.info("Update film: {}", newFilm.getName());
        return filmService.update(newFilm);
    }

    @GetMapping("/director/{directorId}")
    public Collection<Film> getDirectorFilms(@PathVariable("directorId") Long directorId, @RequestParam(value = "sortBy", defaultValue = "likes") String sortBy) {
        return filmService.getDirectorFilms(directorId, sortBy);
    }

    @GetMapping("/search")
    public Collection<Film> getFilmsSearch(@RequestParam(value = "query", required = false) String searchVal, @RequestParam(value = "by", required = false) String searchFields) {
        log.info("Get films Search: {} - query, {} - by", searchVal, searchFields);
        return filmService.getFilmsSearch(searchVal, searchFields);
    }

    @DeleteMapping("/{filmId}")
    public Film removeFilm(@PathVariable("filmId") Long filmId) {
        log.info("Remove film by ID: {}", filmId);
        return filmService.removeFilm(filmId);
    }

    @GetMapping("/common")
    public Collection<Film> getCommonFilms(@RequestParam Long userId, @RequestParam Long friendId) {
        log.info("Get common films: {} {} - Started", userId, friendId);
        return filmService.getCommonFilms(userId, friendId);
    }
}
