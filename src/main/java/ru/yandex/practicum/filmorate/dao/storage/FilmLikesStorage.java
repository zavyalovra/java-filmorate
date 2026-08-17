package ru.yandex.practicum.filmorate.dao.storage;

public interface FilmLikesStorage {
    void addLike(Long filmId, Long userId);

    void removeLike(Long filmId, Long userId);

    long getLikesForFilm(Long filmId);
}
