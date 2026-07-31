package ru.yandex.practicum.filmorate.model;

import lombok.Data;

@Data
public class FilmGenre {

    private final Long filmId;

    private final Long genreId;
}
