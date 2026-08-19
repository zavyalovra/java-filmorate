package ru.yandex.practicum.filmorate.dao.jdbc;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import ru.yandex.practicum.filmorate.exception.InternalServerException;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

public abstract class BaseDbStorage<T> {
    protected final JdbcTemplate jdbc;
    protected final RowMapper<T> mapper;

    protected BaseDbStorage(JdbcTemplate jdbc, RowMapper<T> mapper) {
        this.jdbc = jdbc;
        this.mapper = mapper;
    }

    protected Long findValue(String query, Object... params) {
        return jdbc.queryForObject(query, Long.class, params);
    }

    protected Optional<T> findOne(String query, Object... params) {
        try {
            T result = jdbc.queryForObject(query, mapper, params);
            return Optional.ofNullable(result);
        } catch (EmptyResultDataAccessException ignored) {
            return Optional.empty();
        }
    }

    protected List<T> findMany(String query, Object... params) {
        return jdbc.query(query, mapper, params);
    }

    protected <R> List<R> queryMany(String query, RowMapper<R> rowMapper, Object... params) {
        return jdbc.query(query, rowMapper, params);
    }

    protected <T> T queryMany(String query, ResultSetExtractor<T> extractor, Object... params) {
        return jdbc.query(query, extractor, params);
    }

    protected boolean delete(String query, long id) {
        int rowsDeleted = jdbc.update(query, id);
        return rowsDeleted > 0;
    }

    protected void update(String query, Object... params) {
        int rowsUpdated = jdbc.update(query, params);
        if (rowsUpdated == 0) {
            throw new InternalServerException("Не удалось обновить данные");
        }
    }

    protected void execute(String query, Object... params) {
        jdbc.update(query, params);
    }

    protected long insert(String query, Object... params) {
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection
                    .prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            for (int idx = 0; idx < params.length; idx++) {
                ps.setObject(idx + 1, params[idx]);
            }
            return ps;
        }, keyHolder);

        Long id = keyHolder.getKeyAs(Long.class);

        // Возвращаем id нового пользователя
        if (id != null) {
            return id;
        } else {
            throw new InternalServerException("Не удалось сохранить данные");
        }
    }

    protected String placeholder(int size) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < size; i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append("?");
        }

        return sb.toString();
    }

    protected Integer count(String query, Object... params) {
        return jdbc.queryForObject(query, Integer.class, params);
    }
}
