package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserService {
    private final UserStorage userStorage;

    @Autowired
    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public void addFriend(Long userId, Long friendId) {
        User user = userStorage.get().stream()
                .filter(u -> u.getId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new ValidationException("Пользователь с id = " + userId + " не найден"));

        User friend = userStorage.get().stream()
                .filter(u -> u.getId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new ValidationException("Пользователь с id = " + userId + " не найден"));

        if (user.getFriends() == null) {
            user.setFriends(new HashSet<>());
        }
        if (friend.getFriends() == null) {
            friend.setFriends(new HashSet<>());
        }

        user.getFriends().add(friendId);
        friend.getFriends().add(userId);
    }

    public void removeFriend(Long userId, Long friendId) {
        User user = userStorage.get().stream()
                .filter(u -> u.getId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new ValidationException("Пользователь с id = " + userId + " не найден"));

        User friend = userStorage.get().stream()
                .filter(u -> u.getId().equals(friendId))
                .findFirst()
                .orElseThrow(() -> new ValidationException("Пользователь с id = " + friendId + " не найден"));

        if (user.getFriends() != null) {
            user.getFriends().remove(friendId);
        }
        if (friend.getFriends() != null) {
            friend.getFriends().remove(userId);
        }
    }

    public Set<Long> getFriends(Long userId) {
        Set<Long> friends = userStorage.get().stream()
                .filter(u -> u.getId().equals(userId))
                .findFirst()
                .map(User::getFriends)
                .orElseThrow(() -> new ValidationException("Пользователь с id = " + userId + " не найден"));

        return new HashSet<>(friends);
    }

    public Set<Long> getCommonFriends(Long userId1, Long userId2) {
        Set<Long> friends1 = userStorage.get().stream()
                .filter(u -> u.getId().equals(userId1))
                .findFirst()
                .map(User::getFriends)
                .orElseThrow(() -> new ValidationException("Пользователь с id = " + userId1 + " не найден"));

        Set<Long> friends2 = userStorage.get().stream()
                .filter(u -> u.getId().equals(userId2))
                .findFirst()
                .map(User::getFriends)
                .orElseThrow(() -> new ValidationException("Пользователь с id = " + userId2 + " не найден"));

        return friends1.stream()
                .filter(friends2::contains)
                .collect(Collectors.toSet());
    }
}
