package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {

    private final Map<Long, User> users = new HashMap<>();

    @GetMapping
    public Collection<User> findAll() {
        return users.values();
    }

    @PostMapping
    public User create(@Valid @RequestBody User user) {

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
        return user;
    }

    @PutMapping
    public User update(@Valid @RequestBody User newUser) {

        if (newUser.getId() == null) {
            throw new ValidationException("Id должен быть указан");
        }

        if (users.containsKey(newUser.getId())) {
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
        throw new NotFoundException("Пользователь с id = " + newUser.getId() + " не найден");
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
