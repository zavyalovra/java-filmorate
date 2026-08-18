package ru.yandex.practicum.filmorate.dao.memory;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dao.storage.FilmStorage;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.*;

@Slf4j
@Component
@Qualifier("filmInMemoryStorage")
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Long, Film> films = new HashMap<>();

    @Override
    public boolean deleteFilm(Long id) {
        if (!films.containsKey(id)) {
            throw new NotFoundException("Фильм с id = " + id + " не найден");
        }
        films.remove(id);
        log.info("Фильм с id = {} удален", id);
        return true;
    }

    @Override
    public Collection<Film> get() {
        return films.values();
    }

    @Override
    public Film create(Film film) {
        boolean isTitleExists = films.values().stream()
                .anyMatch(name -> name.getName().equals(film.getName()));

        if (isTitleExists) {
            throw new ValidationException("Этот фильм уже существует");
        }

        film.setId(getNextId());
        log.info("Создание фильма: {}", film);
        films.put(film.getId(), film);

        return film;
    }

    @Override
    public Film update(Film film) {
        if (film.getId() == null) {
            throw new ValidationException("Id должен быть указан");
        }
        findById(film.getId())
                .orElseThrow(() -> new NotFoundException("Фильм с id = " + film.getId() + " не найден"));

        log.info("Обновление фильма: {}", film);
        films.put(film.getId(), film);
        return films.get(film.getId());
    }

    @Override
    public Optional<Film> findById(Long id) {
        return Optional.ofNullable(films.get(id));
    }

    @Override
    public Collection<Film> getPopular(int count) {
        throw new NotFoundException("Метод getPopular не поддерживается в InMemoryFilmStorage");
    }

    private long getNextId() {
        long currentMaxId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
