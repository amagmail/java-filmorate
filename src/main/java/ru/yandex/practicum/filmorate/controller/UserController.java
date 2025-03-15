package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Feed;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.Collection;
import java.util.Set;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    @PutMapping("/{userId}/friends/{friendId}")
    public Set<Long> setFriend(@PathVariable("userId") Long userId, @PathVariable("friendId") Long friendId) {
        log.info("Set friend: {} - userId, {} - friendId", userId, friendId);
        return userService.setFriend(userId, friendId);
    }

    @DeleteMapping("/{userId}/friends/{friendId}")
    public Set<Long> removeFriend(@PathVariable("userId") Long userId, @PathVariable("friendId") Long friendId) {
        log.info("Remove friend: {} - userId, {} - friendId", userId, friendId);
        return userService.removeFriend(userId, friendId);
    }

    @GetMapping("/{userId}/friends")
    public Collection<User> getUserFriends(@PathVariable("userId") Long userId) {
        log.info("Get user friends: {} - userId", userId);
        return userService.getUserFriends(userId);
    }

    @GetMapping("/{userId}/friends/common/{otherId}")
    public Collection<User> getMutualFriends(@PathVariable("userId") Long userId, @PathVariable("otherId") Long otherId) {
        log.info("Get mutual friends: {} - userId, {} - otherUserId", userId, otherId);
        return userService.getMutualFriends(userId, otherId);
    }

    @GetMapping("/{userId}")
    public User getItem(@PathVariable("userId") Long userId) {
        log.info("Get user by ID: {} - userId", userId);
        return userService.getItem(userId);
    }

    @GetMapping
    public Collection<User> getItems() {
        return userService.getItems();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public User create(@Valid @RequestBody User user) {
        log.info("Create user: {}", user.getName());
        return userService.create(user);
    }

    @PutMapping
    public User update(@Valid @RequestBody User newUser) {
        log.info("Update user: {} ", newUser);
        return userService.update(newUser);
    }

    @DeleteMapping("/{userId}")
    public User removeUser(@PathVariable("userId") Long userId) {
        log.info("Remove user by ID: {} ", userId);
        return userService.removeUser(userId);
    }

    @GetMapping("/{userId}/feed")
    public Collection<Feed> getFeed(@PathVariable("userId") Long userId) {
        log.info("Get feed: {} - userId", userId);
        return userService.getFeed(userId);
    }

    @GetMapping("/{userId}/recommendations")
    public Collection<Film> getRecommendations(@PathVariable("userId") Long userId) {
        log.info("Get recommendations: {} - userId", userId);
        return userService.getRecommendations(userId);
    }
}
