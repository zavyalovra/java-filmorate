package ru.yandex.practicum.filmorate.dao.jdbc;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dao.storage.GenreStorage;
import ru.yandex.practicum.filmorate.dao.mappers.GenreRowMapper;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
@Qualifier("genreDbStorage")
public class GenreDbStorage extends BaseDbStorage<Genre> implements GenreStorage {

    public GenreDbStorage(JdbcTemplate jdbc, GenreRowMapper mapper) {
        super(jdbc, mapper);
    }

    @Override
    public Collection<Genre> getByIds(Set<Long> ids) {
        String genreIds = ids.stream()
                .map(id -> "?")
                .collect(Collectors.joining(","));

        String findByIdQuery  = String.format("SELECT * FROM genres WHERE id IN (%s)", genreIds);
        return findMany(findByIdQuery, ids.toArray());
    }
}
