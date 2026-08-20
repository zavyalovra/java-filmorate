package ru.yandex.practicum.filmorate.dao.jdbc;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dao.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.dao.storage.FilmStorage;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.FilmByField;
import ru.yandex.practicum.filmorate.model.FilmSortField;

import java.util.*;
import java.util.stream.Collectors;

@Repository
@Qualifier("filmDbStorage")
public class FilmDbStorage extends BaseDbStorage<Film> implements FilmStorage {
    private static final String SEARCH_BY = """
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
            WHERE %s
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
                   COUNT(DISTINCT fl.user_id) AS likes
            FROM films f
            LEFT JOIN mpa m ON f.mpa_id = m.id
            LEFT JOIN film_likes fl ON f.id = fl.film_id
            LEFT JOIN film_genres as fg ON fg.film_id = f.id
            WHERE %s
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
    private static final String GET_COMMON_FILMS_QUERY = """
            SELECT f.*,
                   m.name AS mpa_name
            FROM films f
            LEFT JOIN film_directors fd ON f.id = fd.film_id
            LEFT JOIN mpa m ON f.mpa_id = m.id
            WHERE f.id IN (
                SELECT fl1.film_id
                FROM film_likes fl1
                JOIN film_likes fl2 ON fl1.film_id = fl2.film_id
                WHERE fl1.user_id = ? AND fl2.user_id = ?
            )
            """;

    private static final String FIND_BY_IDS_QUERY = """
            SELECT f.id,
                   f.name,
                   f.description,
                   f.release_date,
                   f.duration,
                   m.id   AS mpa_id,
                   m.name AS mpa_name
            FROM films f
            LEFT JOIN mpa m ON f.mpa_id = m.id
            WHERE f.id IN (%s)
            """;

    public FilmDbStorage(JdbcTemplate jdbc, FilmRowMapper mapper) {
        super(jdbc, mapper);
    }

    @Override
    public Collection<Film> getCommonFilms(Long userId, Long friendId) {
        return findMany(GET_COMMON_FILMS_QUERY, userId, friendId);
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

    @Override
    public Collection<Film> getPopular(int count, Long genreId, Integer year) {
        List<String> condition = new ArrayList<>();
        List<Object> params = new ArrayList<>();

        if (genreId != null) {
            condition.add("fg.genre_id = ?");
            params.add(genreId);
        }
        if (year != null) {
            condition.add("EXTRACT(YEAR FROM f.release_date) = ?");
            params.add(year);
        }
        params.add(count);
        //"5=5" так как форматирование требует чтобы это не было пустым
        String where = condition.isEmpty() ? "5=5" : String.join(" AND ", condition);
        String finalQuery = GET_POPULAR_QUERY.formatted(where);
        return findMany(finalQuery,params.toArray());
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
    public Collection<Film> getByIds(Set<Long> genresIds) {
        return findMany(FIND_BY_IDS_QUERY.formatted(placeholder(genresIds.size())), genresIds.toArray());
    }

    @Override
    public Collection<Film> search(String query, List<FilmByField> by) {
        String pattern = "%" + query.toLowerCase() + "%";
        String byDirectorAndOrTitle = by.stream()
                .map(FilmByField::getSqlField)
                .collect(Collectors.joining(" OR "));
        String finalQuery = SEARCH_BY.formatted(byDirectorAndOrTitle);
        Object[] params = new Object[by.size()];
        Arrays.fill(params, pattern);
        return findMany(finalQuery, params);
    }

    @Override
    public Collection<Film> getByDirector(Long directorId, List<FilmSortField> sortBy) {
        String orderBy = sortBy.stream()
                .map(FilmSortField::getSqlField)
                .collect(Collectors.joining(", "));

        String query = GET_BY_DIRECTOR_QUERY.formatted(orderBy);

        return findMany(query, directorId);
    }
}
