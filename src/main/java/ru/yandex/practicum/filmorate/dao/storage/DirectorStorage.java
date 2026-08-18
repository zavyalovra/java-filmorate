package ru.yandex.practicum.filmorate.dao.storage;

import ru.yandex.practicum.filmorate.model.Director;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface DirectorStorage {
    Collection<Director> get();

    Optional<Director> getById(Long id);

    Collection<Director> getByIds(Set<Long> directorsIds);

    Map<Long, Set<Director>> getDirectorsForFilms(Set<Long> filmIds);

    void saveDirectorsForFilm(Long filmId, Collection<Director> directors);

    void removeDirector(Long directorId);
}
