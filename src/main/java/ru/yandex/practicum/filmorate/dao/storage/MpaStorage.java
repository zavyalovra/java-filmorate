package ru.yandex.practicum.filmorate.dao.storage;

import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.Optional;

public interface MpaStorage {
    Optional<Mpa> findById(Long id);
}
