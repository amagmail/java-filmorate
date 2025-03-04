package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.director.DirectorStorage;

import java.util.Collection;

@RequiredArgsConstructor
@Service
public class DirectorService {

    private final DirectorStorage directorStorage;

    public Collection<Director> getItems() {
        return directorStorage.getItems();
    }

    public Director getItem(Long directorId) {
        return directorStorage.getItem(directorId);
    }

    public Director create(Director director) {
        return directorStorage.create(director);
    }

    public Director update(Director director) {
        if (director.getId() == null) {
            throw new ValidationException("Поле id содержит невалидное значение");
        }
        return directorStorage.update(director);
    }

    public Boolean remove(Long directorId) {
        return directorStorage.remove(directorId);
    }

}
