package ru.yandex.practicum.filmorate.dao.storage;

import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

public interface GenreStorage {
    Collection<Genre> get();

    Optional<Genre> getById(Long id);

    Collection<Genre> getByIds(Set<Long> ids);
}
