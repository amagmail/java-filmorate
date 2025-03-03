package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;

import java.util.Collection;

@RequiredArgsConstructor
@Service
public class GenreService {

    private final GenreStorage genreStorage;

    public Collection<Genre> getItems() {
        return genreStorage.getItems();
    }

    public Genre getItem(Long genreId) {
        return genreStorage.getItem(genreId);
    }

}
