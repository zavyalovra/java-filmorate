package ru.yandex.practicum.filmorate.dao.jdbc;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dao.mappers.FilmLikesRowMapper;
import ru.yandex.practicum.filmorate.dao.storage.FilmLikesStorage;
import ru.yandex.practicum.filmorate.model.FilmLike;

@Repository
@Qualifier("filmLikesDbStorage")
public class FilmLikesDbStorage extends BaseDbStorage<FilmLike> implements FilmLikesStorage {
    private static final String ADD_RATE_QUERY = "MERGE INTO film_likes(film_id, user_id) KEY (film_id, user_id) VALUES (?, ?)";
    private static final String REMOVE_RATE_QUERY = "DELETE FROM film_likes WHERE film_id = ? AND user_id = ?";
    private static final String GET_LIKES_QUERY = "SELECT COUNT(*) FROM film_likes WHERE film_id = ?";

    public FilmLikesDbStorage(JdbcTemplate jdbc, FilmLikesRowMapper mapper) {
        super(jdbc, mapper);
    }

    @Override
    public void addLike(Long filmId, Long userId) {
        execute(ADD_RATE_QUERY, filmId, userId);
    }

    @Override
    public void removeLike(Long filmId, Long userId) {
        execute(REMOVE_RATE_QUERY, filmId, userId);
    }

    @Override
    public long getLikesForFilm(Long filmId) {
        return findValue(GET_LIKES_QUERY, filmId);
    }
}
