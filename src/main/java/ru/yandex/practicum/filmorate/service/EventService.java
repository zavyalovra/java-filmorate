package ru.yandex.practicum.filmorate.service;


import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.storage.EventStorage;
import ru.yandex.practicum.filmorate.dao.storage.UserStorage;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Event;

import java.util.Collection;

@Service
public class EventService {
    private final EventStorage eventStorage;
    private final UserStorage userStorage;


    public EventService(EventStorage eventStorage,
                        @Qualifier("userDbStorage") UserStorage userStorage) {
        this.eventStorage = eventStorage;
        this.userStorage = userStorage;
    }

    public void createEvent(Long userId, Event.EventType eventType, Event.Operation operation, Long entityId) {
        Event event = new Event();
        event.setTimestamp(System.currentTimeMillis());
        event.setUserId(userId);
        event.setEventType(eventType);
        event.setOperation(operation);
        event.setEntityId(entityId);

        eventStorage.addEvent(event);
    }

    public Collection<Event> getFeed(Long userId) {
        userStorage.findById(userId).orElseThrow(()
                -> new NotFoundException("Пользователь с id = " + userId + " не найден"));
        return eventStorage.getFeed(userId);
    }
}
