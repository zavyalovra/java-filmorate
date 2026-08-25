package ru.yandex.practicum.filmorate.dao.storage;

import ru.yandex.practicum.filmorate.model.User;
import java.util.Collection;
import java.util.Optional;

public interface UserStorage {
    Collection<User> get();

    User create(User user);

    User update(User user);

    Optional<User> findById(Long id);

    void addFriend(Long userId, Long friendId);

    void removeFriend(Long userId, Long friendId);

    Collection<User> getFriends(Long userId);

    Collection<User> getCommonFriends(Long userId, Long otherUserId);

    boolean deleteUser(Long userId);
}