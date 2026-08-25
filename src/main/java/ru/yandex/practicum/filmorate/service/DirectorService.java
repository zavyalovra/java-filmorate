package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.storage.DirectorStorage;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Director;

import java.util.Collection;

@Service
public class DirectorService {
    private final DirectorStorage directorStorage;

    @Autowired
    public DirectorService(@Qualifier("directorDbStorage") DirectorStorage directorStorage) {
        this.directorStorage = directorStorage;
    }

    public Collection<Director> findAll() {
        return directorStorage.get();
    }

    public Director findDirectorById(Long id) {
        return directorStorage.getById(id)
                .orElseThrow(() -> new NotFoundException("Режиссер с id = " + id + " не найден"));
    }

    public Director create(Director director) {
        return directorStorage.create(director);
    }

    public Director update(Director director) {
        if (director.getId() == null) {
            throw new ValidationException("Должен быть задан id режиссера");
        }

        if (directorStorage.getById(director.getId()).isEmpty()) {
            throw new NotFoundException(String.format("Режиссер с id = %s не найден", director.getId()));
        }

        return directorStorage.update(director);
    }

    public void delete(Long directorId) {
        Director director = findDirectorById(directorId);
        directorStorage.delete(director.getId());
    }
}
