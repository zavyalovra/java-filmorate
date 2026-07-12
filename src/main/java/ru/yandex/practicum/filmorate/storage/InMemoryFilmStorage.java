package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class InMemoryFilmStorage implements FilmStorage {

    private final Map<Long, Film> films = new HashMap<>();

    @Override
    public Collection<Film> get() {
        return films.values();
    }

    @Override
    public void create(Film film) {
        boolean isTitleExists = films.values().stream()
                .anyMatch(name -> name.getName().equals(film.getName()));

        if (isTitleExists) {
            throw new ValidationException("Этот фильм уже существует");
        }

        film.setId(getNextId());
        log.info("Создание фильма: {}", film);
        films.put(film.getId(), film);
    }

    @Override
    public Film update(Film film) {
        if (film.getId() == null) {
            throw new ValidationException("Id должен быть указан");
        }

        if (!films.containsKey(film.getId())) {
            throw new NotFoundException("Фильм с id " + film.getId() + " не найден");
        }

        log.info("Обновление фильма: {}", film);
        films.put(film.getId(), film);
        return films.get(film.getId());
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
