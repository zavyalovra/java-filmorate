package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.jdbc.FilmLikesDbStorage;
import ru.yandex.practicum.filmorate.dao.jdbc.MpaDbStorage;
import ru.yandex.practicum.filmorate.dao.storage.FilmGenreStorage;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.*;
import ru.yandex.practicum.filmorate.dao.storage.FilmStorage;
import ru.yandex.practicum.filmorate.dao.storage.UserStorage;
import ru.yandex.practicum.filmorate.dao.storage.GenreStorage;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final GenreStorage genreStorage;
    private final FilmGenreStorage filmGenreStorage;
    private final MpaDbStorage mpaDbStorage;
    private final FilmLikesDbStorage filmLikesDbStorage;

    @Autowired
    public FilmService(
            @Qualifier("filmDbStorage") FilmStorage filmStorage,
            @Qualifier("userDbStorage") UserStorage userStorage,
            @Qualifier("genreDbStorage") GenreStorage genreStorage,
            @Qualifier("filmGenreDbStorage") FilmGenreStorage filmGenreStorage,
            @Qualifier("mpaDbStorage") MpaDbStorage mpaDbStorage,
            @Qualifier("filmLikesDbStorage") FilmLikesDbStorage filmLikesDbStorage) {

        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.genreStorage = genreStorage;
        this.filmGenreStorage = filmGenreStorage;
        this.mpaDbStorage = mpaDbStorage;
        this.filmLikesDbStorage = filmLikesDbStorage;
    }

    public Collection<Film> findAll() {
        Collection<Film> films = filmStorage.get();

        for (Film film : films) {
            Set<Long> genreIds = filmGenreStorage.getGenresForFilm(film.getId()).stream()
                    .map(FilmGenre::getGenreId)
                    .collect(Collectors.toSet());
            Collection<Genre> genres = genreStorage.getByIds(genreIds);
            film.setGenres(genres);
        }

        return films;
    }

    public Film create(Film film) {
        Mpa mpa = mpaDbStorage.findById(film.getMpa().getId())
                .orElseThrow(() -> new NotFoundException("Рейтинг с id = " + film.getMpa().getId() + " не найден"));
        film.setMpa(mpa);

        Set<Long> genreIds = film.getGenres().stream()
                .map(Genre::getId)
                .collect(Collectors.toSet());
        Collection<Genre> genres = genreStorage.getByIds(genreIds);

        if (genres.size() != genreIds.size()) {
            throw new NotFoundException("Один или несколько жанров не существуют");
        }

        film.setGenres(genres);
        Film created = filmStorage.create(film);
        filmGenreStorage.saveGenresForFilm(created.getId(), created.getGenres());

        return findFilmById(created.getId());
    }

    public Film update(Film film) {
        Film updatedFilm = filmStorage.update(film);

        Set<Long> genreIds = film.getGenres().stream()
                .map(Genre::getId)
                .collect(Collectors.toSet());

        Collection<Genre> genres = genreStorage.getByIds(genreIds);
        filmGenreStorage.saveGenresForFilm(updatedFilm.getId(), genres);
        updatedFilm.setGenres(genres);

        return updatedFilm;
    }

    public Film findFilmById(Long id) {
        Film film = filmStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Фильм с id = " + id + " не найден"));

        Set<Long> genreIds = filmGenreStorage.getGenresForFilm(id).stream()
                .map(FilmGenre::getGenreId)
                .collect(Collectors.toSet());

        Collection<Genre> genres = genreStorage.getByIds(genreIds);
        film.setGenres(genres);

        return film;
    }

    public void addRate(Long filmId, Long userId) {
        Film film = filmStorage.findById(filmId)
                .orElseThrow(() -> new NotFoundException("Фильм с id = " + filmId + " не найден"));
        User user = userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + userId + " не найден"));

        filmLikesDbStorage.addLike(filmId, userId);
    }

    public void removeRate(Long filmId, Long userId) {
        Film film = filmStorage.findById(filmId)
                .orElseThrow(() -> new NotFoundException("Фильм с id = " + filmId + " не найден"));
        User user = userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + userId + " не найден"));

        filmLikesDbStorage.removeLike(filmId, userId);
    }

    public Collection<Film> getPopularFilms(int count) {
        Collection<Film> films = filmStorage.getPopular(count);

        for (Film film : films) {
            Set<Long> genreIds = filmGenreStorage.getGenresForFilm(film.getId()).stream()
                    .map(FilmGenre::getGenreId)
                    .collect(Collectors.toSet());
            Collection<Genre> genres = genreStorage.getByIds(genreIds);
            film.setGenres(genres);
        }

        return films;
    }

    private int getRatingCount(Film film) {
        return film.getRating() != null ? film.getRating().size() : 0;
    }
}
