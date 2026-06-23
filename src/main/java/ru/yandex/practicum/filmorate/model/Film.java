package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.time.LocalDate;

/**
 * Film.
 */
@Data
public class Film {

    private Long id;

    @NotBlank(message = "Название не может быть пустым")
    private String name;

    @Size(max = 200, message = "Максимальная длина описания — 200 символов")
    private String description;

    private LocalDate releaseDate;

    @Positive(message = "Продолжительность фильма должна быть положительным числом")
    private int duration;

    @AssertTrue(message = "Дата релиза — не раньше 28 декабря 1895 года")
    public boolean isReleaseDateValid() {
        if (releaseDate == null) {
            return false;
        }
        LocalDate minReleaseDate = LocalDate.of(1895, 12, 28);
        return !releaseDate.isBefore(minReleaseDate);
    }
}
