package ru.yandex.practicum.filmorate.dao.jdbc;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dao.storage.UserStorage;
import ru.yandex.practicum.filmorate.dao.mappers.UserRowMapper;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.model.UserFriendship.Status;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
@Qualifier("userDbStorage")
public class UserDbStorage extends BaseDbStorage<User> implements UserStorage {
    private static final String FIND_ALL_QUERY = "SELECT * FROM users";
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM users WHERE id = ?";
    private static final String INSERT_QUERY = "INSERT INTO users(email, login, name, birthday)" +
            "VALUES (?, ?, ?, ?)";
    private static final String UPDATE_QUERY = "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? " +
            "WHERE id = ?";
    private static final String GET_FRIENDS_STATUS_QUERY = """
            SELECT status
            FROM user_friendships
            WHERE user_id = ?
              AND friend_id = ?
            """;
    private static final String UPDATE_FRIEND_QUERY = """
            MERGE INTO user_friendships(user_id, friend_id, status)
            KEY (user_id, friend_id)
            VALUES (?, ?, ?)
            """;

    private static final String DELETE_FRIEND_QUERY = """
            DELETE
            FROM user_friendships
            WHERE user_id = ?
              AND friend_id = ?;
            """;

    private static final String GET_FRIENDS_QUERY = """
            SELECT u.*
            FROM users u
            JOIN user_friendships uf ON u.id = uf.friend_id
            WHERE uf.user_id = ?
            """;
    private static final String GET_COMMON_FRIENDS_QUERY = """
            SELECT u.*
            FROM users u
            JOIN user_friendships f1 ON u.id = f1.friend_id
            JOIN user_friendships f2 ON u.id = f2.friend_id
            WHERE f1.user_id = ?
              AND f2.user_id = ?
            """;

    public UserDbStorage(JdbcTemplate jdbc, UserRowMapper mapper) {
        super(jdbc, mapper);
    }

    @Override
    public Collection<User> get() {
        return findMany(FIND_ALL_QUERY);
    }

    @Override
    public User create(User user) {
        long id = insert(
                INSERT_QUERY,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday()
        );
        user.setId(id);
        return user;
    }

    @Override
    public User update(User user) {
        update(
                UPDATE_QUERY,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday(),
                user.getId()
        );
        return user;
    }

    @Override
    public Optional<User> findById(Long id) {
        return findOne(FIND_BY_ID_QUERY, id);
    }

    @Override
    public void addFriend(Long userId, Long friendId) {
        Optional<Status> friendshipStatus = getFriendshipStatus(friendId, userId);

        if (friendshipStatus.isPresent()) {
            update(UPDATE_FRIEND_QUERY, userId, friendId, Status.CONFIRMED.name());
            update(UPDATE_FRIEND_QUERY, friendId, userId, Status.CONFIRMED.name());
            return;
        }
        update(UPDATE_FRIEND_QUERY, userId, friendId, Status.PENDING.name());
    }

    @Override
    public void removeFriend(Long userId, Long friendId) {
        Optional<Status> friendshipStatus = getFriendshipStatus(userId, friendId);

        if (friendshipStatus.isPresent() && friendshipStatus.get() == Status.CONFIRMED) {
            update(UPDATE_FRIEND_QUERY, friendId, userId, Status.PENDING.name());
            execute(DELETE_FRIEND_QUERY, userId, friendId);
            return;
        }
        execute(DELETE_FRIEND_QUERY, userId, friendId);
    }

    @Override
    public Collection<User> getFriends(Long userId) {
        return findMany(GET_FRIENDS_QUERY, userId);
    }

    @Override
    public Collection<User> getCommonFriends(Long userId, Long otherUserId) {
        return findMany(GET_COMMON_FRIENDS_QUERY, userId, otherUserId);
    }

    private Optional<Status> getFriendshipStatus(Long userId, Long friendId) {
        List<String> statuses = queryMany(
                GET_FRIENDS_STATUS_QUERY,
                (rs, rowNum) -> rs.getString("status"),
                userId,
                friendId
        );

        return statuses.isEmpty() ? Optional.empty() : Optional.of(Status.valueOf(statuses.getFirst()));
    }
}
