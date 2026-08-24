package ru.yandex.practicum.filmorate.dao.jdbc;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dao.mappers.DirectorRowMapper;
import ru.yandex.practicum.filmorate.dao.storage.DirectorStorage;
import ru.yandex.practicum.filmorate.model.Director;

import java.util.*;

@Repository
@Qualifier("directorDbStorage")
public class DirectorDbStorage extends BaseDbStorage<Director> implements DirectorStorage {
    private static final String INSERT_QUERY = "MERGE INTO film_directors(film_id, director_id) KEY (film_id, director_id)" +
            "VALUES (?, ?)";
    private static final String FIND_ALL_QUERY = "SELECT * FROM directors";
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM directors WHERE id = ?";
    private static final String FIND_DIRECTORS_FOR_FILMS = """
            SELECT  fd.film_id,
                    d.id AS director_id,
                    d.name AS director_name
            FROM film_directors fd
            JOIN directors d ON fd.director_id = d.id
            WHERE fd.film_id IN (%s)
            ORDER BY fd.film_id, d.id
            """;
    private static final String FIND_BY_IDS_QUERY = "SELECT * FROM directors WHERE id IN (%s)";
    private static final String INSERT_DIRECTOR_QUERY = "INSERT INTO directors(name) VALUES (?)";
    private static final String UPDATE_DIRECTOR_QUERY = "UPDATE directors SET name = ? WHERE id = ?";
    private static final String DELETE_DIRECTOR_QUERY = "DELETE FROM directors WHERE id = ?";

    public DirectorDbStorage(JdbcTemplate jdbc, DirectorRowMapper mapper) {
        super(jdbc, mapper);
    }

    @Override
    public Collection<Director> get() {
        return findMany(FIND_ALL_QUERY);
    }

    @Override
    public Optional<Director> getById(Long id) {
        return findOne(FIND_BY_ID_QUERY, id);
    }

    @Override
    public Collection<Director> getByIds(Set<Long> directorsIds) {
        return findMany(FIND_BY_IDS_QUERY.formatted(placeholder(directorsIds.size())), directorsIds.toArray());
    }

    @Override
    public Map<Long, Set<Director>> getDirectorsForFilms(Set<Long> filmIds) {
        if (filmIds.isEmpty()) {
            return Collections.emptyMap();
        }

        return queryMany(FIND_DIRECTORS_FOR_FILMS.formatted(placeholder(filmIds.size())), rs -> {
            Map<Long, Set<Director>> result = new HashMap<>();

            while (rs.next()) {
                long filmId = rs.getLong("film_id");
                result.computeIfAbsent(filmId, id -> new LinkedHashSet<>())
                        .add(new Director(rs.getLong("director_id"), rs.getString("director_name")));
            }

            return result;
        }, filmIds.toArray());
    }

    @Override
    public void saveDirectorsForFilm(Long filmId, Collection<Director> directors) {
        jdbc.update("DELETE FROM film_directors WHERE film_id = ?", filmId);

        for (Director director : directors) {
            execute(
                    INSERT_QUERY,
                    filmId,
                    director.getId()
            );
        }
    }

    @Override
    public Director create(Director director) {
        long id = insert(
                INSERT_DIRECTOR_QUERY,
                director.getName()
        );
        director.setId(id);

        return director;
    }

    @Override
    public Director update(Director director) {
        update(
                UPDATE_DIRECTOR_QUERY,
                director.getName(),
                director.getId()
        );

        return director;
    }

    @Override
    public void delete(Long directorId) {
        execute(
                DELETE_DIRECTOR_QUERY,
                directorId
        );
    }
}
