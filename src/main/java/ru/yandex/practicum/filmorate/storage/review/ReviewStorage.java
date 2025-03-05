package ru.yandex.practicum.filmorate.storage.review;

import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.Storage;

import java.util.Collection;

public interface ReviewStorage extends Storage<Review> {

    Boolean remove(Long reviewId);

    Collection<Review> getItems(Long filmId, Integer count);

    Review addLike(Long reviewId, Long userId);

    Review addDislike(Long reviewId, Long userId);

    Review removeLike(Long reviewId, Long userId);

    Review removeDislike(Long reviewId, Long userId);

}
