package ru.yandex.practicum.filmorate.dao.storage;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.FilmSortField;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface FilmStorage {
    Collection<Film> get();

    Film create(Film film);

    Film update(Film film);

    Optional<Film> findById(Long id);

    Collection<Film> getPopular(int count);

    boolean deleteFilm(Long id);

    Collection<Film> getByDirector(Long directorId, List<FilmSortField> sortBy);
    Collection<Film> search(String title, boolean byTitle, boolean byDirector);

}