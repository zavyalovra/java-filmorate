package ru.yandex.practicum.filmorate.dao.storage;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.FilmSortField;
import ru.yandex.practicum.filmorate.model.FilmByField;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface FilmStorage {
    Collection<Film> get();

    Film create(Film film);

    Film update(Film film);

    Optional<Film> findById(Long id);

    Collection<Film> getPopular(int count);

    boolean deleteFilm(Long id);

    Collection<Film> getByDirector(Long directorId, List<FilmSortField> sortBy);

    Collection<Film> getByIds(Set<Long> genresIds);

    Collection<Film> search(String query, List<FilmByField> by);
}