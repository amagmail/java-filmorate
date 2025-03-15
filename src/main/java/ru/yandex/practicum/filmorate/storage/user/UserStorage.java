package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.Feed;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.Storage;

import java.util.Collection;
import java.util.Set;

public interface UserStorage extends Storage<User> {

    Set<Long> setFriend(Long userId, Long friendId);

    Set<Long> removeFriend(Long userId, Long friendId);

    Collection<User> getUserFriends(Long userId);

    Collection<User> getMutualFriends(Long userId, Long otherId);

    User removeUser(Long userId);

    void clearAllFriends(Long userId);

    Collection<Feed> getFeed(Long userId);
}