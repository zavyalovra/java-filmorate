package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.util.Set;

@Data
@EqualsAndHashCode(of = { "email", "login" })
public class User {
    private Long id;

    @Email(message = "Некорректный формат email")
    private String email;

    @Pattern(
            regexp = "^[^\\s]+$",
            message = "Логин не может быть пустым или содержать пробелы"
    )
    private String login;

    private String name;

    @PastOrPresent(message = "Дата рождения не может быть в будущем")
    private LocalDate birthday;

    private Set<Long> friends;
}
