package ru.yandex.practicum.filmorate.dao.storage;

import ru.yandex.practicum.filmorate.model.Review;

import java.util.Collection;
import java.util.Optional;

public interface ReviewStorage {

    Review create(Review review);

    Review update(Review review);

    void delete(Long reviewId);

    Optional<Review> findById(Long reviewId);

    Collection<Review> getByCount(int count);

    Collection<Review> getByFilmAndCount(Long filmId, int count);

    void addLike(Long reviewId, Long userId);

    void addDisLike(Long reviewId, Long userId);

    void removeLike(Long reviewId, Long userId);

    void removeDisLike(Long reviewId, Long userId);
}
