package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    @Autowired
    public FilmService(FilmStorage filmStorage, UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public Collection<Film> findAll() {
        return filmStorage.get();
    }

    public void create(Film film) {
        filmStorage.create(film);
    }

    public Film update(Film film) {
        return filmStorage.update(film);
    }

    public Optional<Film> findFilmById(Long id) {
        return filmStorage.get().stream()
                .filter(f -> f.getId().equals(id))
                .findFirst();
    }

    public void addRate(Long filmId, Long userId) {
        Film film = filmStorage.get().stream()
                .filter(f -> f.getId().equals(filmId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Фильм с id = " + filmId + " не найден"));

        User user = userStorage.get().stream()
                .filter(u -> u.getId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + userId + " не найден"));

        if (film.getRating() == null) {
            film.setRating(new HashSet<>());
        }

        film.getRating().add(userId);
    }

    public void removeRate(Long filmId, Long userId) {
        Film film = filmStorage.get().stream()
                .filter(f -> f.getId().equals(filmId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Фильм с id = " + filmId + " не найден"));

        User user = userStorage.get().stream()
                .filter(u -> u.getId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + userId + " не найден"));

        if (film.getRating() != null) {
            film.getRating().remove(userId);
        }
    }

    public Collection<Film> getPopular(int count) {
        return filmStorage.get().stream()
                .sorted((film1, film2) -> Integer.compare(getRatingCount(film2), getRatingCount(film1)))
                .limit(count)
                .collect(Collectors.toList());
    }

    private int getRatingCount(Film film) {
        return film.getRating() != null ? film.getRating().size() : 0;
    }
}
