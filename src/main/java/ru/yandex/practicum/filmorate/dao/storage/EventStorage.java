package ru.yandex.practicum.filmorate.dao.storage;

import ru.yandex.practicum.filmorate.model.Event;

import java.util.Collection;

public interface EventStorage {

    void addEvent(Event event);

    Collection<Event> getFeed(Long userId);
}
