package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.service.ReviewService;

import java.util.Collection;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping("/{reviewId}")
    public Review getItem(@PathVariable("reviewId") Long reviewId) {
        log.info("Get review by ID: {}", reviewId);
        return reviewService.getItem(reviewId);
    }

    @PutMapping("/{reviewId}/like/{userId}")
    public Review addLike(@PathVariable("reviewId") Long reviewId, @PathVariable("userId") Long userId) {
        log.info("Add like to the review with reviewID = {} from a user with userId = {}", reviewId, userId);
        return reviewService.addLike(reviewId, userId);
    }

    @PutMapping("/{reviewId}/dislike/{userId}")
    public Review addDislike(@PathVariable("reviewId") Long reviewId, @PathVariable("userId") Long userId) {
        log.info("Add DIS like to the review with reviewID = {} from a user with userId = {}", reviewId, userId);
        return reviewService.addDislike(reviewId, userId);
    }

    @DeleteMapping("/{reviewId}/like/{userId}")
    public Review removeLike(@PathVariable("reviewId") Long reviewId, @PathVariable("userId") Long userId) {
        log.info("Remove like to the review with reviewID = {} from a user with userId = {}", reviewId, userId);
        return reviewService.removeLike(reviewId, userId);
    }

    @DeleteMapping("/{reviewId}/dislike/{userId}")
    public Review removeDislike(@PathVariable("reviewId") Long reviewId, @PathVariable("userId") Long userId) {
        log.info("Remove DIS like to the review with reviewID = {} from a user with userId = {}", reviewId, userId);
        return reviewService.removeDislike(reviewId, userId);
    }

    @GetMapping
    public Collection<Review> getItems(@RequestParam(required = false) Long filmId, @RequestParam(defaultValue = "10", required = false) Integer count) {
        return reviewService.getItems(filmId, count);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Review create(@Valid @RequestBody Review review) {
        log.info("Create review {} ", review);
        return reviewService.create(review);
    }

    @PutMapping
    public Review update(@Valid @RequestBody Review newReview) {
        log.info("Update review {} ", newReview);
        return reviewService.update(newReview);
    }

    @DeleteMapping("/{reviewId}")
    public Boolean remove(@PathVariable("reviewId") Long reviewId) {
        log.info("Remove review by ID: {}", reviewId);
        return reviewService.remove(reviewId);
    }

}
