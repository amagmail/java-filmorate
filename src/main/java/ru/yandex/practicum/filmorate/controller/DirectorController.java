package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.service.DirectorService;

import java.util.Collection;

@RequiredArgsConstructor
@RestController
@RequestMapping("/directors")
public class DirectorController {

    private final DirectorService directorService;

    @GetMapping("/{directorId}")
    public Director getItem(@PathVariable("directorId") Long genreId) {
        return directorService.getItem(genreId);
    }

    @GetMapping
    public Collection<Director> getItems() {
        return directorService.getItems();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Director create(@Valid @RequestBody Director director) {
        return directorService.create(director);
    }

    @PutMapping
    public Director update(@Valid @RequestBody Director newDirector) {
        return directorService.update(newDirector);
    }

    @DeleteMapping("/{directorId}")
    public Boolean remove(@PathVariable("directorId") Long directorId) {
        return directorService.remove(directorId);
    }

}
