package ru.yandex.practicum.filmorate.dao.jdbc;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dao.mappers.EventRowMapper;
import ru.yandex.practicum.filmorate.dao.storage.EventStorage;
import ru.yandex.practicum.filmorate.model.Event;

import java.util.Collection;

@Repository
public class EventDbStorage extends BaseDbStorage<Event> implements EventStorage {

    private static final String INSERT_QUERY = """
            INSERT INTO events (timestamp, user_id, event_type, operation, entity_id)
            VALUES (?, ?, ?, ?, ?)
            """;
    private static final String FIND_FEEDS_QUERY = "SELECT * FROM events WHERE user_id = ?";

    public EventDbStorage(JdbcTemplate jdbc, EventRowMapper mapper) {
        super(jdbc, mapper);
    }

    @Override
    public void addEvent(Event event) {
        long id = insert(INSERT_QUERY,
                event.getTimestamp(),
                event.getUserId(),
                event.getEventType().name(),
                event.getOperation().name(),
                event.getEntityId()
        );

        event.setEventId(id);
    }

    @Override
    public Collection<Event> getFeed(Long userId) {
        return findMany(FIND_FEEDS_QUERY, userId);
    }
}
