package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class InMemoryUserStorage implements UserStorage {

    private final Map<Long, User> users = new HashMap<>();

    @Override
    public Collection<User> get() {
        return users.values();
    }

    @Override
    public void create(User user) {
        boolean isLoginExists = users.values().stream()
                .anyMatch(login -> login.getLogin().equals(user.getLogin()));
        if (isLoginExists) {
            throw new ValidationException("Этот логин уже используется");
        }

        if (user.getEmail() == null) {
            throw new ValidationException("Электронная почта не может быть пустой");
        }

        boolean isEmailExists = users.values().stream()
                .anyMatch(email -> email.getEmail().equals(user.getEmail()));

        if (isEmailExists) {
            throw new ValidationException("Этот имейл уже используется");
        }

        user.setId(getNextId());
        user.setName(user.getName() == null || user.getName().isBlank() ? user.getLogin() : user.getName());

        log.info("Создание пользователя: {}", user);
        users.put(user.getId(), user);
    }

    @Override
    public User update(User newUser) {
        if (newUser.getId() == null) {
            throw new ValidationException("Id должен быть указан");
        }

        if (!users.containsKey(newUser.getId())) {
            throw new NotFoundException("Пользователь с id = " + newUser.getId() + " не найден");
        }

        User oldUser = users.get(newUser.getId());

        if (newUser.getLogin() != null) {
            boolean isLoginExists = users.values().stream()
                    .anyMatch(user ->
                            !user.getId().equals(newUser.getId()) && user.getLogin().equals(newUser.getLogin())
                    );

            if (isLoginExists) {
                throw new ValidationException("Этот логин уже используется");
            }
            oldUser.setLogin(newUser.getLogin());
        }

        if (newUser.getEmail() != null) {
            boolean isEmailExists = users.values().stream()
                    .anyMatch(user ->
                            !user.getId().equals(newUser.getId()) && user.getEmail().equals(newUser.getEmail())
                    );

            if (isEmailExists) {
                throw new ValidationException("Этот имейл уже используется");
            }
            oldUser.setEmail(newUser.getEmail());
        }

        if (newUser.getBirthday() != null) {
            oldUser.setBirthday(newUser.getBirthday());
        }

        if (newUser.getName() != null) {
            oldUser.setName(newUser.getName().isBlank() ? oldUser.getLogin() : newUser.getName());
        }

        log.info("Обновление данных пользователя: {}", oldUser);
        return oldUser;
    }

    @Override
    public User findUserById(Long id) {
        if (!users.containsKey(id)) {
            throw new NotFoundException("Пользователь с id = " + id + " не найден");
        }
        return users.get(id);
    }

    private long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
