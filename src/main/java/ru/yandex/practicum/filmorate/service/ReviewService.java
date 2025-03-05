package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.review.ReviewStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;

@RequiredArgsConstructor
@Service
public class ReviewService {

    private final ReviewStorage reviewStorage;
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    public Collection<Review> getItems(Long filmId, Integer count) {
        return reviewStorage.getItems(filmId, count);
    }

    public Review getItem(Long reviewId) {
        return reviewStorage.getItem(reviewId);
    }

    public Review create(Review review) {
        if (filmStorage.getItem(review.getFilmId()) == null) {
            throw new NotFoundException("Фильм не найден");
        }
        if (userStorage.getItem(review.getUserId()) == null) {
            throw new NotFoundException("Пользователь не найден");
        }
        return reviewStorage.create(review);
    }

    public Review update(Review review) {
        if (filmStorage.getItem(review.getFilmId()) == null) {
            throw new NotFoundException("Фильм не найден");
        }
        if (userStorage.getItem(review.getUserId()) == null) {
            throw new NotFoundException("Пользователь не найден");
        }
        if (review.getReviewId() == null) {
            throw new ValidationException("Поле id содержит невалидное значение");
        }
        return reviewStorage.update(review);
    }

    public Boolean remove(Long reviewId) {
        return reviewStorage.remove(reviewId);
    }

    public Review addLike(Long reviewId, Long userId) {
        if (reviewStorage.getItem(reviewId) == null) {
            throw new ValidationException("Отзыв не найден");
        }
        if (userStorage.getItem(userId) == null) {
            throw new NotFoundException("Пользователь не найден");
        }
        return reviewStorage.addLike(reviewId, userId);
    }

    public Review removeLike(Long reviewId, Long userId) {
        if (reviewStorage.getItem(reviewId) == null) {
            throw new ValidationException("Отзыв не найден");
        }
        if (userStorage.getItem(userId) == null) {
            throw new NotFoundException("Пользователь не найден");
        }
        return reviewStorage.removeLike(reviewId, userId);
    }

    public Review addDislike(Long reviewId, Long userId) {
        if (reviewStorage.getItem(reviewId) == null) {
            throw new ValidationException("Отзыв не найден");
        }
        if (userStorage.getItem(userId) == null) {
            throw new NotFoundException("Пользователь не найден");
        }
        return reviewStorage.addDislike(reviewId, userId);
    }

    public Review removeDislike(Long reviewId, Long userId) {
        if (reviewStorage.getItem(reviewId) == null) {
            throw new ValidationException("Отзыв не найден");
        }
        if (userStorage.getItem(userId) == null) {
            throw new NotFoundException("Пользователь не найден");
        }
        return reviewStorage.removeDislike(reviewId, userId);
    }

}
