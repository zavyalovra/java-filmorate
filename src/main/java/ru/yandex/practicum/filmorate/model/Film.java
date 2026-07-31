package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Film.
 */
@Data
public class Film {

    private Long id;

    @NotBlank(message = "Название не может быть пустым")
    private String name;

    @NotNull(message = "Описание не может быть null")
    @Size(max = 200, message = "Максимальная длина описания — 200 символов")
    private String description;

    private LocalDate releaseDate;

    @Positive(message = "Продолжительность фильма должна быть положительным числом")
    private int duration;

    @JsonIgnore
    @AssertTrue(message = "Дата релиза — не раньше 28 декабря 1895 года")
    public boolean isReleaseDateValid() {
        if (releaseDate == null) {
            return false;
        }
        LocalDate minReleaseDate = LocalDate.of(1895, 12, 28);
        return !releaseDate.isBefore(minReleaseDate);
    }

    private Set<Long> rating = new HashSet<>();

    private Collection<Genre> genres = new ArrayList<>();

    private Mpa mpa;
}
