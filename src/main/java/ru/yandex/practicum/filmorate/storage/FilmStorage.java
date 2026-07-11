package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;

public interface FilmStorage {
    Collection<Film> get();

    void create(Film film);

    Film update(Film film);

    void delete(Film film);
}