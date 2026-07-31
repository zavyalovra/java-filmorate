package ru.yandex.practicum.filmorate.dao.jdbc;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dao.mappers.FilmGenreRowMapper;
import ru.yandex.practicum.filmorate.dao.storage.FilmGenreStorage;
import ru.yandex.practicum.filmorate.model.FilmGenre;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Collection;

@Repository
@Qualifier("filmGenreDbStorage")
public class FilmGenreDbStorage extends BaseDbStorage<FilmGenre> implements FilmGenreStorage {
    private static final String INSERT_QUERY = "INSERT INTO film_genres(film_id, genre_id)" +
            "VALUES (?, ?)";

    public FilmGenreDbStorage(JdbcTemplate jdbc, FilmGenreRowMapper mapper) {
        super(jdbc, mapper);
    }

    @Override
    public Collection<FilmGenre> getGenresForFilm(Long filmId) {
        String findByIdQuery  = "SELECT * FROM film_genres WHERE film_id = ?";
        return findMany(findByIdQuery, filmId);
    }

    @Override
    public void saveGenresForFilm(Long filmId, Collection<Genre> genres) {
        for (Genre genre : genres) {
            update(
                    INSERT_QUERY,
                    filmId,
                    genre.getId()
            );
        }
    }
}
