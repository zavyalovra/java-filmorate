package ru.yandex.practicum.filmorate.dao.storage;

import ru.yandex.practicum.filmorate.model.FilmLike;

import java.util.Collection;

public interface FilmLikesStorage {
    void addLike(Long filmId, Long userId);

    void removeLike(Long filmId, Long userId);

    long getLikesForFilm(Long filmId);

    Collection<FilmLike> get();
}
