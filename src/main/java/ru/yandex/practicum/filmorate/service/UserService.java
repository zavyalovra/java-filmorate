package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.storage.EventStorage;
import ru.yandex.practicum.filmorate.dao.storage.UserStorage;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;

@Slf4j
@Service
public class UserService {
    private final UserStorage userStorage;
    private final EventService eventService;

    @Autowired
    public UserService(
            @Qualifier("userDbStorage") UserStorage userStorage,
            EventService eventService) {

        this.userStorage = userStorage;
        this.eventService = eventService;
    }

    public Collection<User> findAll() {
        return userStorage.get();
    }

    public User create(User user) {
        return userStorage.create(user);
    }

    public User update(User user) {
        findUserById(user.getId());
        return userStorage.update(user);
    }

    public User findUserById(Long id) {
        return userStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + id + " не найден"));
    }

    public void addFriend(Long userId, Long friendId) {
        User user = userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + userId + " не найден"));
        User friend = userStorage.findById(friendId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + friendId + " не найден"));

        log.info("Отправляем запрос от userId = {} на добавление в друзья friendId = {}", userId, friendId);
        userStorage.addFriend(user.getId(), friend.getId());

        eventService.createEvent(userId, Event.EventType.FRIEND, Event.Operation.ADD, friendId);
    }

    public void removeFriend(Long userId, Long friendId) {
        User user = userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + userId + " не найден"));
        User friend = userStorage.findById(friendId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + friendId + " не найден"));

        log.info("Удаление заявки userId = {} на добавление в друзья friendId = {}", userId, friendId);
        userStorage.removeFriend(user.getId(), friend.getId());

        eventService.createEvent(userId, Event.EventType.FRIEND, Event.Operation.REMOVE, friendId);
    }

    public Collection<User> getFriends(Long userId) {
        User user = userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + userId + " не найден"));

        return userStorage.getFriends(user.getId());
    }

    public Collection<User> getCommonFriends(Long userId, Long otherUserId) {
        User user = userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + userId + " не найден"));
        User otherUser = userStorage.findById(otherUserId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + otherUserId + " не найден"));

        return userStorage.getCommonFriends(user.getId(), otherUser.getId());
    }

    public void deleteUser(Long userId) {
        userStorage.deleteUser(userId);
        log.info("Удаление пользователя с id = {}", userId);
    }
}
