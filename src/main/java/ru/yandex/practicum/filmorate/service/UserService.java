package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Feed;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.*;

@RequiredArgsConstructor
@Service
public class UserService {

    private final UserStorage userStorage;
    private final FilmStorage filmStorage;

    public Set<Long> setFriend(Long userId, Long friendId) {
        return userStorage.setFriend(userId, friendId);
    }

    public Set<Long> removeFriend(Long userId, Long friendId) {
        return userStorage.removeFriend(userId, friendId);
    }

    public Collection<User> getUserFriends(Long userId) {
        return userStorage.getUserFriends(userId);
    }

    public Collection<User> getMutualFriends(Long userId, Long otherId) {
        return userStorage.getMutualFriends(userId, otherId);
    }

    public User create(User user) {
        return userStorage.create(user);
    }

    public User update(User user) {
        if (user.getId() == null) {
            throw new ValidationException("Поле id содержит невалидное значение");
        }
        return userStorage.update(user);
    }

    public Collection<User> getItems() {
        return userStorage.getItems();
    }

    public User getItem(Long userId) {
        return userStorage.getItem(userId);
    }

    public User removeUser(Long userId) {
        return userStorage.removeUser(userId);
    }

    public Collection<Feed> getFeed(Long userId) {
        if (userStorage.getItem(userId) == null) {
            throw new NotFoundException("Не удалось найти пользователя с id " + userId);
        }
        return userStorage.getFeed(userId);
    }

    public Collection<Film> getRecommendations(Long userId) {
        List<Long> similarUserIds = filmStorage.findSimilarUsers(userId);

        Set<Long> recommendedFilmIds = new HashSet<>();
        for (Long similarUserId : similarUserIds) {
            recommendedFilmIds.addAll(filmStorage.findFilmsLikedByUserButNotTarget(similarUserId, userId));
        }

        if (recommendedFilmIds.isEmpty()) {
            return Collections.emptyList();
        }
        return filmStorage.getFilmsByIds(new ArrayList<>(recommendedFilmIds));
    }
}
