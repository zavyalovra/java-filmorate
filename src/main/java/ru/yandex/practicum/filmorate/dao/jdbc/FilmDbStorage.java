package ru.yandex.practicum.filmorate.dao.jdbc;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dao.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.dao.storage.FilmStorage;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.FilmSortField;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@Qualifier("filmDbStorage")
public class FilmDbStorage extends BaseDbStorage<Film> implements FilmStorage {
    private static final String SEARCH_BY_TITLE = """
            SELECT f.id,
                   f.name,
                   f.description,
                   f.release_date,
                   f.duration,
                   m.id   AS mpa_id,
                   m.name AS mpa_name,
                   COUNT(fl.user_id) AS likes
            FROM films f
            LEFT JOIN film_directors as fd ON fd.film_id = f.id
            LEFT JOIN directors as d ON d.id = fd.director_id
            LEFT JOIN mpa as m ON f.mpa_id = m.id
            LEFT JOIN film_likes as fl ON fl.film_id = f.id
            WHERE f.name LIKE ?
            GROUP BY f.id
            ORDER BY likes DESC
            """;

    private static final String SEARCH_BY_TITLE_AND_DIRECTOR = """
            SELECT f.id,
                   f.name,
                   f.description,
                   f.release_date,
                   f.duration,
                   m.id   AS mpa_id,
                   m.name AS mpa_name,
                   COUNT(fl.user_id) AS likes
            FROM films f
            LEFT JOIN film_directors as fd ON fd.film_id = f.id
            LEFT JOIN directors as d ON d.id = fd.director_id
            LEFT JOIN mpa as m ON f.mpa_id = m.id
            LEFT JOIN film_likes as fl ON fl.film_id = f.id
            WHERE f.name LIKE ? OR d.name LIKE ?
            GROUP BY f.id
            ORDER BY likes DESC
            """;

    private static final String SEARCH_BY_DIRECTOR = """
            SELECT f.id,
                   f.name,
                   f.description,
                   f.release_date,
                   f.duration,
                   m.id   AS mpa_id,
                   m.name AS mpa_name,
                   COUNT(fl.user_id) AS likes
            FROM films f
            LEFT JOIN film_directors as fd ON fd.film_id = f.id
            LEFT JOIN directors as d ON d.id = fd.director_id
            LEFT JOIN mpa as m ON f.mpa_id = m.id
            LEFT JOIN film_likes as fl ON fl.film_id = f.id
            WHERE d.name LIKE ?
            GROUP BY f.id
            ORDER BY likes DESC
            """;


    private static final String FIND_ALL_QUERY = """
            SELECT f.id,
                   f.name,
                   f.description,
                   f.release_date,
                   f.duration,
                   m.id   AS mpa_id,
                   m.name AS mpa_name
            FROM films f
            LEFT JOIN mpa m ON f.mpa_id = m.id
            """;
    private static final String FIND_BY_ID_QUERY = """
            SELECT f.id,
                   f.name,
                   f.description,
                   f.release_date,
                   f.duration,
                   m.id   AS mpa_id,
                   m.name AS mpa_name
            FROM films f
            LEFT JOIN mpa m ON f.mpa_id = m.id
            WHERE f.id = ?
            """;
    private static final String INSERT_QUERY = "INSERT INTO films(name, description, release_date, duration, mpa_id) " +
            "VALUES (?, ?, ?, ?, ?)";
    private static final String UPDATE_QUERY = "UPDATE films SET name = ?, description = ?, release_date = ?, " +
            "duration = ?, mpa_id = ? WHERE id = ?";
    private static final String GET_POPULAR_QUERY = """
            SELECT f.*,
                   m.name AS mpa_name,
                   COUNT(fl.user_id) AS likes
            FROM films f
            LEFT JOIN mpa m ON f.mpa_id = m.id
            LEFT JOIN film_likes fl ON f.id = fl.film_id
            GROUP BY f.id
            ORDER BY likes DESC, f.id
            LIMIT ?
            """;
    private static final String GET_BY_DIRECTOR_QUERY = """
            SELECT f.*,
                   m.name AS mpa_name,
                   COUNT(fl.user_id) AS likes
            FROM films f
            LEFT JOIN film_directors fd ON f.id = fd.film_id
            LEFT JOIN mpa m ON f.mpa_id = m.id
            LEFT JOIN film_likes fl ON f.id = fl.film_id
            WHERE fd.director_id = ?
            GROUP BY f.id
            ORDER BY %s
            """;

    private static final String DELETE_QUERY = "DELETE FROM films WHERE id = ?";

    public FilmDbStorage(JdbcTemplate jdbc, FilmRowMapper mapper) {
        super(jdbc, mapper);
    }

    @Override
    public boolean deleteFilm(Long id) {
        return delete(DELETE_QUERY, id);
    }

    @Override
    public Collection<Film> get() {
        return findMany(FIND_ALL_QUERY);
    }

    @Override
    public Film create(Film film) {
        long id = insert(
                INSERT_QUERY,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa().getId()
        );
        film.setId(id);

        return film;
    }

    @Override
    public Film update(Film film) {
        update(
                UPDATE_QUERY,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa().getId(),
                film.getId()
        );
        return film;
    }

    @Override
    public Optional<Film> findById(Long id) {
        return findOne(FIND_BY_ID_QUERY, id);
    }

    public Collection<Film> getPopular(int count) {
        return findMany(GET_POPULAR_QUERY, count);
    }

    @Override
    public Collection<Film> getByDirector(Long directorId, List<FilmSortField> sortBy) {
        String orderBy = sortBy.stream()
                .map(FilmSortField::getSqlField)
                .collect(Collectors.joining(", "));

        String query = GET_BY_DIRECTOR_QUERY.formatted(orderBy);

        return findMany(query, directorId);
    }

    @Override
    public Collection<Film> search(String query, boolean byTile, boolean byDirector) {
        String pattern = "%" + query + "%";
        if(byTile && byDirector) {
            return findMany(SEARCH_BY_TITLE_AND_DIRECTOR, pattern,pattern);
        } else if (byDirector) {
            return findMany(SEARCH_BY_DIRECTOR, pattern);
        } else if (byTile) {
            return findMany(SEARCH_BY_TITLE, pattern);
        }
        else{
            throw new ValidationException("Параметр by должен содержать title или director");
        }
    }
}
