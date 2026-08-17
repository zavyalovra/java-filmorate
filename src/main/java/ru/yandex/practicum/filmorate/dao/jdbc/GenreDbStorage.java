package ru.yandex.practicum.filmorate.dao.jdbc;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dao.storage.GenreStorage;
import ru.yandex.practicum.filmorate.dao.mappers.GenreRowMapper;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.*;

@Repository
@Qualifier("genreDbStorage")
public class GenreDbStorage extends BaseDbStorage<Genre> implements GenreStorage {
    private static final String FIND_ALL_QUERY = "SELECT * FROM genres";
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM genres WHERE id = ?";
    private static final String FIND_GENRES_FOR_FILMS = """
            SELECT  fg.film_id,
                    g.id AS genre_id,
                    g.name AS genre_name
            FROM film_genres fg
            JOIN genres g ON fg.genre_id = g.id
            WHERE fg.film_id IN (%s)
            ORDER BY fg.film_id, g.id
            """;
    private static final String FIND_BY_IDS_QUERY = "SELECT * FROM genres WHERE id IN (%s)";

    public GenreDbStorage(JdbcTemplate jdbc, GenreRowMapper mapper) {
        super(jdbc, mapper);
    }

    @Override
    public Collection<Genre> get() {
        return findMany(FIND_ALL_QUERY);
    }

    @Override
    public Optional<Genre> getById(Long id) {
        return findOne(FIND_BY_ID_QUERY, id);
    }

    @Override
    public Collection<Genre> getByIds(Set<Long> genresIds) {
        return findMany(FIND_BY_IDS_QUERY.formatted(placeholder(genresIds.size())), genresIds.toArray());
    }

    @Override
    public Map<Long, Set<Genre>> getGenresForFilms(Set<Long> filmIds) {
        if (filmIds.isEmpty()) {
            return Collections.emptyMap();
        }

        return queryMany(FIND_GENRES_FOR_FILMS.formatted(placeholder(filmIds.size())), rs -> {
            Map<Long, Set<Genre>> result = new HashMap<>();

            while (rs.next()) {
                long filmId = rs.getLong("film_id");
                result.computeIfAbsent(filmId, id -> new LinkedHashSet<>())
                        .add(new Genre(rs.getLong("genre_id"), rs.getString("genre_name")));
            }

            return result;
        }, filmIds.toArray());
     }
}
