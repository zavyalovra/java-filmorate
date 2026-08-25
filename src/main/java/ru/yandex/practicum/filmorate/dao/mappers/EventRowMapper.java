package ru.yandex.practicum.filmorate.dao.mappers;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Event;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
@RequiredArgsConstructor
public class EventRowMapper implements RowMapper<Event> {

    @Override
    public Event mapRow(ResultSet rs, int rowNum) throws SQLException {
        Event event = new Event();
        event.setEventId(rs.getLong("event_id"));
        event.setTimestamp(rs.getLong("timestamp"));
        event.setUserId(rs.getLong("user_id"));
        event.setEventType(Event.EventType.valueOf(rs.getString("event_type")));
        event.setOperation(Event.Operation.valueOf(rs.getString("operation")));
        event.setEntityId(rs.getLong("entity_id"));

        return event;
    }
}
