package ru.yandex.practicum.filmorate.model;

import lombok.Data;

@Data
public class FilmLike {
    private Long filmId;
    private Long userId;

    public FilmLike(Long filmId, Long userId) {
        this.filmId = filmId;
        this.userId = userId;
    }
}
