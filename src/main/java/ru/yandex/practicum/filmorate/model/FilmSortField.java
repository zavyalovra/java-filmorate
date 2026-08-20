package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum FilmSortField {
    year("f.release_date ASC"),
    likes("likes DESC");

    private final String sqlField;
}
