package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;

import java.util.Collection;

@RequiredArgsConstructor
@Service
public class MpaService {

    private final MpaStorage mpaStorage;

    public Collection<Mpa> getItems() {
        return mpaStorage.getItems();
    }

    public Mpa getItem(Long mpaId) {
        return mpaStorage.getItem(mpaId);
    }

}
