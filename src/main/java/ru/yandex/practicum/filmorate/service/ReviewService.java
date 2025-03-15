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
        validateReviewData(null, null, review);
        return reviewStorage.create(review);
    }

    public Review update(Review review) {
        if (review.getReviewId() == null) {
            throw new ValidationException("Поле id содержит невалидное значение");
        }
        validateReviewData(null, null, review);
        return reviewStorage.update(review);
    }

    public Boolean remove(Long reviewId) {
        return reviewStorage.remove(reviewId);
    }

    public Review addLike(Long reviewId, Long userId) {
        validateReviewData(reviewId, userId,null);
        return reviewStorage.addLike(reviewId, userId);
    }

    public Review removeLike(Long reviewId, Long userId) {
        validateReviewData(reviewId, userId,null);
        return reviewStorage.removeLike(reviewId, userId);
    }

    public Review addDislike(Long reviewId, Long userId) {
        validateReviewData(reviewId, userId,null);
        return reviewStorage.addDislike(reviewId, userId);
    }

    public Review removeDislike(Long reviewId, Long userId) {
        validateReviewData(reviewId, userId, null);
        return reviewStorage.removeDislike(reviewId, userId);
    }

    private void validateReviewData(Long reviewId, Long userId, Review review) {
        if (reviewId != null && reviewStorage.getItem(reviewId) == null) {
            throw new ValidationException("Отзыв не найден");
        }
        if (userId != null && userStorage.getItem(userId) == null) {
            throw new NotFoundException("Пользователь не найден");
        }
        if (review != null && filmStorage.getItem(review.getFilmId()) == null) {
            throw new NotFoundException("Фильм не найден");
        }
        if (review != null && userStorage.getItem(review.getUserId()) == null) {
            throw new NotFoundException("Пользователь не найден");
        }
    }
}
