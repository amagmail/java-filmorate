package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.service.DirectorService;

import java.util.Collection;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/directors")
public class DirectorController {

    private final DirectorService directorService;

    @GetMapping("/{directorId}")
    public Director getItem(@PathVariable("directorId") Long genreId) {
        log.info("get director by ID: {}", genreId);
        return directorService.getItem(genreId);
    }

    @GetMapping
    public Collection<Director> getItems() {
        return directorService.getItems();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Director create(@Valid @RequestBody Director director) {
        log.info("Create director {} ", director);
        return directorService.create(director);
    }

    @PutMapping
    public Director update(@Valid @RequestBody Director newDirector) {
        log.info("Update director {} was started", newDirector);
        return directorService.update(newDirector);
    }

    @DeleteMapping("/{directorId}")
    public Boolean remove(@PathVariable("directorId") Long directorId) {
        log.info("Remove director by ID: {}", directorId);
        return directorService.remove(directorId);
    }

}
