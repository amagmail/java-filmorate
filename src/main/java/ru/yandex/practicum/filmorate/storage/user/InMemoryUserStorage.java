package ru.yandex.practicum.filmorate.storage.user;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;

@Component
public class InMemoryUserStorage implements UserStorage {

    private final Map<Long, User> users = new HashMap<>();

    @Override
    public Set<Long> setFriend(Long userId, Long friendId) {
        User user1 = getItem(userId);
        User user2 = getItem(friendId);
        user1.getFriends().add(user2.getId());
        user2.getFriends().add(user1.getId());
        return user1.getFriends();
    }

    @Override
    public Set<Long> removeFriend(Long userId, Long friendId) {
        User user1 = getItem(userId);
        User user2 = getItem(friendId);
        user1.getFriends().remove(user2.getId());
        user2.getFriends().remove(user1.getId());
        return user1.getFriends();
    }

    @Override
    public Collection<User> getUserFriends(Long userId) {
        return getItem(userId).getFriends().stream()
                .map(users::get)
                .toList();
    }

    @Override
    public Collection<User> getMutualFriends(Long userId, Long otherId) {
        User user1 = getItem(userId);
        User user2 = getItem(otherId);
        Set<Long> mutualFriends = new HashSet<>(user1.getFriends());
        mutualFriends.retainAll(user2.getFriends());
        return mutualFriends.stream()
                .map(users::get)
                .toList();
    }

    @Override
    public User create(User entity) {
        if (entity.getName() == null || entity.getName().isBlank()) {
            entity.setName(entity.getLogin());
        }
        entity.setId(getNextId());
        users.put(entity.getId(), entity);
        return entity;
    }

    @Override
    public User update(User entity) {
        if (users.containsKey(entity.getId())) {
            User oldUser = users.get(entity.getId());
            if (entity.getEmail() != null && !entity.getEmail().isBlank()) {
                oldUser.setEmail(entity.getEmail());
            }
            if (entity.getLogin() != null && !entity.getLogin().isBlank()) {
                oldUser.setLogin(entity.getLogin());
            }
            if (entity.getName() != null && !entity.getName().isBlank()) {
                oldUser.setName(entity.getName());
            }
            if (entity.getBirthday() != null) {
                oldUser.setBirthday(entity.getBirthday());
            }
            return oldUser;
        }
        throw new NotFoundException("Пользователь с идентификатором " + entity.getId() + " не найден");
    }

    @Override
    public Collection<User> getItems() {
        return users.values();
    }

    @Override
    public User getItem(Long userId) {
        if (!users.containsKey(userId)) {
            throw new NotFoundException("Пользователя с идентификатором " + userId + " не существует");
        }
        return users.get(userId);
    }

    // Вспомогательный метод для генерации идентификатора объекта
    private long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }

    @Override
    public User removeUser(Long userId) {
        if (userId == null) {
            throw new ValidationException("ID пользователя пуст. Введите значение и повторите попытку.");
        }
        if (!users.containsKey(userId)) {
            throw new NotFoundException("Пользователь с ID: " + userId + " не найден!");
        }
        return users.remove(userId);
    }

    @Override
    public void clearAllFriends(Long userId) {
        User user1 = getItem(userId);
        user1.getFriends().clear();
    }
}