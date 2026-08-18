package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.storage.ReviewStorage;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Review;

import java.util.Collection;

@Slf4j
@Service
public class ReviewService {

    private final ReviewStorage reviewStorage;
    private final FilmService filmService;
    private final UserService userService;

    @Autowired
    public ReviewService(ReviewStorage reviewStorage, FilmService filmService, UserService userService) {
        this.reviewStorage = reviewStorage;
        this.filmService = filmService;
        this.userService = userService;
    }

    public Review create(Review review) {
        filmService.findFilmById(review.getFilmId());
        userService.findUserById(review.getUserId());
        return reviewStorage.create(review);
    }

    public Review update(Review review) {
        log.info("Обновляем reviewId = {}", review.getReviewId());
        findById(review.getReviewId());
        filmService.findFilmById(review.getFilmId());
        userService.findUserById(review.getUserId());
        return reviewStorage.update(review);
    }

    public void delete(Long reviewId) {
        reviewStorage.delete(reviewId);
    }

    public Review findById(Long reviewId) {
        return reviewStorage.findById(reviewId).orElseThrow(() -> new NotFoundException("Отзыв с id = " + reviewId + " не найден"));
    }

    public Collection<Review> getByFilmAndCount(Long filmId, int count) {
        filmService.findFilmById(filmId);
        return reviewStorage.getByFilmAndCount(filmId, count);
    }

    public void addLike(Long reviewId, Long userId) {
        log.info("Ставим like на reviewId = {} от userId = {}", reviewId, userId);
        findById(reviewId);
        userService.findUserById(userId);
        reviewStorage.addLike(reviewId, userId);
    }

    public void addDisLike(Long reviewId, Long userId) {
        log.info("Ставим dislike на reviewId = {} от userId = {}", reviewId, userId);
        findById(reviewId);
        userService.findUserById(userId);
        reviewStorage.addDisLike(reviewId, userId);
    }

    public void removeLike(Long reviewId, Long userId) {
        log.info("Убираем like на reviewId = {} от userId = {}", reviewId, userId);
        findById(reviewId);
        userService.findUserById(userId);
        reviewStorage.removeLike(reviewId, userId);
    }

    public void removeDisLike(Long reviewId, Long userId) {
        log.info("Убираем dislike на reviewId = {} от userId = {}", reviewId, userId);
        findById(reviewId);
        userService.findUserById(userId);
        reviewStorage.removeDisLike(reviewId, userId);
    }
}
