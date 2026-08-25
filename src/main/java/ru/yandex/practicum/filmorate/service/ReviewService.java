package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.storage.ReviewStorage;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.Review;

import java.util.Collection;

@Slf4j
@Service
public class ReviewService {

    private final ReviewStorage reviewStorage;
    private final FilmService filmService;
    private final UserService userService;
    private final EventService eventService;

    @Autowired
    public ReviewService(ReviewStorage reviewStorage, FilmService filmService, UserService userService, EventService eventService) {
        this.reviewStorage = reviewStorage;
        this.filmService = filmService;
        this.userService = userService;
        this.eventService = eventService;
    }

    public Review create(Review review) {
        filmService.findFilmById(review.getFilmId());
        userService.findUserById(review.getUserId());
        Review createdReview = reviewStorage.create(review);
        eventService.createEvent(createdReview.getUserId(), Event.EventType.REVIEW, Event.Operation.ADD, createdReview.getReviewId());
        return createdReview;
    }

    public Review update(Review review) {
        log.info("Обновляем reviewId = {}", review.getReviewId());
        filmService.findFilmById(review.getFilmId());
        userService.findUserById(review.getUserId());
        Review oldReview = findById(review.getReviewId());
        Review updatedReview = reviewStorage.update(review);
        eventService.createEvent(oldReview.getUserId(), Event.EventType.REVIEW, Event.Operation.UPDATE, updatedReview.getReviewId());
        return updatedReview;
    }

    public void delete(Long reviewId) {
        Review review = findById(reviewId);
        eventService.createEvent(review.getUserId(), Event.EventType.REVIEW, Event.Operation.REMOVE, reviewId);
        reviewStorage.delete(reviewId);
    }

    public Review findById(Long reviewId) {
        return reviewStorage.findById(reviewId).orElseThrow(() -> new NotFoundException("Отзыв с id = " + reviewId + " не найден"));
    }

    public Collection<Review> getByFilmAndCount(Long filmId, int count) {
        if (filmId == null) {
            return reviewStorage.getByCount(count);
        }

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
