package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum FilmByField {
    title("LOWER(f.name) LIKE ?"),
    director("LOWER(d.name) LIKE ?");

    private final String sqlField;
}
