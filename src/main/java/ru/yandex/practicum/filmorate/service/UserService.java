package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserService {
    private final UserStorage userStorage;

    @Autowired
    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public Collection<User> findAll() {
        return userStorage.get();
    }

    public User create(User user) {
        return userStorage.create(user);
    }

    public User update(User user) {
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
                        .orElseThrow(() -> new NotFoundException("Фильм с id = " + friendId + " не найден"));

        log.info("Определение userId = {} и friendId = {} друзьями", userId, friendId);
        user.getFriends().add(friendId);
        friend.getFriends().add(userId);
    }

    public void removeFriend(Long userId, Long friendId) {
        User user = userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + userId + " не найден"));
        User friend = userStorage.findById(friendId)
                .orElseThrow(() -> new NotFoundException("Фильм с id = " + friendId + " не найден"));

        user.getFriends().remove(friendId);
        friend.getFriends().remove(userId);

        log.info("Удаление userId = {} и friendId = {} из дружественных связей", userId, friendId);
    }

    public Collection<User> getFriends(Long userId) {
        Set<Long> friends = userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + userId + " не найден"))
                .getFriends();

        return userStorage.get().stream()
                .filter(u -> friends.contains(u.getId()))
                .collect(Collectors.toSet());
    }

    public Collection<User> getCommonFriends(Long userId1, Long userId2) {
        Set<Long> friends1 = userStorage.findById(userId1)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + userId1 + " не найден"))
                .getFriends();
        Set<Long> friends2 = userStorage.findById(userId2)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + userId2 + " не найден"))
                .getFriends();

        Set<Long> friends = friends1.stream()
                .filter(friends2::contains)
                .collect(Collectors.toSet());

        return userStorage.get().stream()
                .filter(u -> friends.contains(u.getId()))
                .collect(Collectors.toSet());
    }
}
