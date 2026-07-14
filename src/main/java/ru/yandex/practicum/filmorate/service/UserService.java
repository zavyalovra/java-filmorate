package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
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

    public void create(User user) {
        userStorage.create(user);
    }

    public User update(User user) {
        return userStorage.update(user);
    }

    public Optional<User> findUserById(Long id) {
        return Optional.ofNullable(userStorage.findUserById(id));
    }

    public void addFriend(Long userId, Long friendId) {
        User user = userStorage.findUserById(userId);
        User friend = userStorage.findUserById(friendId);

        log.info("Определение userId = {} и friendId = {} друзьями", userId, friendId);
        user.getFriends().add(friendId);
        friend.getFriends().add(userId);
    }

    public void removeFriend(Long userId, Long friendId) {
        User user = userStorage.findUserById(userId);
        User friend = userStorage.findUserById(friendId);

        user.getFriends().remove(friendId);
        friend.getFriends().remove(userId);

        log.info("Удаление userId = {} и friendId = {} из дружественных связей", userId, friendId);
    }

    public Collection<User> getFriends(Long userId) {
        Set<Long> friends = userStorage.findUserById(userId).getFriends();

        return userStorage.get().stream()
                .filter(u -> friends.contains(u.getId()))
                .collect(Collectors.toSet());
    }

    public Collection<User> getCommonFriends(Long userId1, Long userId2) {
        Set<Long> friends1 = userStorage.findUserById(userId1).getFriends();
        Set<Long> friends2 = userStorage.findUserById(userId2).getFriends();

        Set<Long> friends = friends1.stream()
                .filter(friends2::contains)
                .collect(Collectors.toSet());

        return userStorage.get().stream()
                .filter(u -> friends.contains(u.getId()))
                .collect(Collectors.toSet());
    }
}
