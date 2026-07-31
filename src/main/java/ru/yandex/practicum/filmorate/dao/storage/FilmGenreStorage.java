package ru.yandex.practicum.filmorate.dao.storage;

import ru.yandex.practicum.filmorate.model.FilmGenre;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Collection;

public interface FilmGenreStorage {
    Collection<FilmGenre> getGenresForFilm(Long filmId);

    void saveGenresForFilm(Long filmId, Collection<Genre> genres);
}
