package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.dao.storage.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.*;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final GenreStorage genreStorage;
    private final FilmGenreStorage filmGenreStorage;
    private final MpaStorage mpaStorage;
    private final FilmLikesStorage filmLikesStorage;
    private final EventService eventService;
    private final DirectorStorage directorStorage;

    @Autowired
    public FilmService(
            @Qualifier("filmDbStorage") FilmStorage filmStorage,
            @Qualifier("userDbStorage") UserStorage userStorage,
            @Qualifier("genreDbStorage") GenreStorage genreStorage,
            @Qualifier("filmGenreDbStorage") FilmGenreStorage filmGenreStorage,
            @Qualifier("mpaDbStorage") MpaStorage mpaStorage,
            @Qualifier("filmLikesDbStorage") FilmLikesStorage filmLikesStorage,
            @Qualifier("directorDbStorage") DirectorStorage directorStorage,
            EventService eventService) {

        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.genreStorage = genreStorage;
        this.filmGenreStorage = filmGenreStorage;
        this.mpaStorage = mpaStorage;
        this.filmLikesStorage = filmLikesStorage;
        this.directorStorage = directorStorage;
        this.eventService = eventService;
    }

    public Collection<Film> findAll() {
        Collection<Film> films = filmStorage.get();

        return addFilmDetails(films);
    }

    @Transactional
    public Film create(Film film) {
        if (film.getMpa() != null) {
            Mpa mpa = mpaStorage.getById(film.getMpa().getId())
                    .orElseThrow(() -> new NotFoundException("Рейтинг с id = " + film.getMpa().getId() + " не найден"));
            film.setMpa(mpa);
        }

        Set<Long> genreIds = film.getGenres().stream().map(Genre::getId).collect(Collectors.toSet());
        Collection<Genre> genres = genreStorage.getByIds(genreIds);

        if (genres.size() != genreIds.size()) {
            throw new NotFoundException("Один или несколько жанров не существуют");
        }

        Set<Long> directorsIds = film.getDirectors().stream().map(Director::getId).collect(Collectors.toSet());
        Collection<Director> directors = directorStorage.getByIds(directorsIds);

        film.setGenres(genres);
        film.setDirectors(directors);
        Film created = filmStorage.create(film);
        filmGenreStorage.saveGenresForFilm(created.getId(), created.getGenres());
        directorStorage.saveDirectorsForFilm(created.getId(), created.getDirectors());

        return findFilmById(created.getId());
    }

    @Transactional
    public Film update(Film film) {
        filmStorage.findById(film.getId())
                .orElseThrow(() -> new NotFoundException("Фильм с id = " + film.getId() + " не найден"));

        if (film.getMpa() != null) {
            Mpa mpa = mpaStorage.getById(film.getMpa().getId())
                    .orElseThrow(() -> new NotFoundException("Рейтинг с id = " + film.getMpa().getId() + " не найден"));
            film.setMpa(mpa);
        }

        Set<Long> genreIds = film.getGenres().stream().map(Genre::getId).collect(Collectors.toSet());
        Collection<Genre> genres = genreStorage.getByIds(genreIds);
        if (genres.size() != genreIds.size()) {
            throw new NotFoundException("Один или несколько жанров не существуют");
        }

        Set<Long> directorsIds = film.getDirectors().stream().map(Director::getId).collect(Collectors.toSet());
        Collection<Director> directors = directorStorage.getByIds(directorsIds);

        Film updatedFilm = filmStorage.update(film);
        filmGenreStorage.saveGenresForFilm(updatedFilm.getId(), genres);
        directorStorage.saveDirectorsForFilm(updatedFilm.getId(), directors);

        updatedFilm.setGenres(genres);
        updatedFilm.setDirectors(directors);

        return updatedFilm;
    }

    public Film findFilmById(Long id) {
        Film film = filmStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Фильм с id = " + id + " не найден"));

        Map<Long, Set<Genre>> genresMap = genreStorage.getGenresForFilms(Set.of(film.getId()));
        Map<Long, Set<Director>> directorsMap = directorStorage.getDirectorsForFilms(Set.of(film.getId()));
        film.setGenres(genresMap.getOrDefault(film.getId(), Set.of()));
        film.setDirectors(directorsMap.getOrDefault(film.getId(), Set.of()));

        return film;
    }

    public void addRate(Long filmId, Long userId) {
        checkUserExist(userId);
        checkFilmExist(filmId);

        filmLikesStorage.addLike(filmId, userId);

        eventService.createEvent(userId, Event.EventType.LIKE, Event.Operation.ADD, filmId);
    }

    public void removeRate(Long filmId, Long userId) {
        checkFilmExist(filmId);
        checkUserExist(userId);

        filmLikesStorage.removeLike(filmId, userId);

        eventService.createEvent(userId, Event.EventType.LIKE, Event.Operation.REMOVE, filmId);
    }

    public Collection<Film> getPopularFilms(int count, Long genreID, Integer year) {
        if (year != null && year < 1895) {
            throw new ValidationException("Год не может быть раньше 1895");
        }
        Collection<Film> films = filmStorage.getPopular(count, genreID, year);

        return addFilmDetails(films);
    }

    public void deleteFilm(Long filmId) {
        filmStorage.deleteFilm(filmId);
    }

    public Collection<Film> getCommonFilms(Long userId, Long friendId) {
        checkUserExist(userId);
        checkUserExist(friendId);

        Collection<Film> commonFilms = filmStorage.getCommonFilms(userId, friendId);

        addFilmDetails(commonFilms);

        return commonFilms.stream()
                .sorted((f1, f2) -> Integer.compare(getRatingCount(f2), getRatingCount(f1)))
                .collect(Collectors.toList());
    }

    private int getRatingCount(Film film) {
        return film.getRating() != null ? film.getRating().size() : 0;
    }

    public Collection<Film> search(String query, List<FilmByField> by) {
        if (query.isBlank()) {
            throw new ValidationException("Параметр query не может быть пустым");
        }
        if (by.isEmpty()) {
            throw new ValidationException("Параметр by не может быть пустым");
        }

        Collection<Film> films = filmStorage.search(query, by);

        return addFilmDetails(films);
    }

    public Collection<Film> findByDirector(Long directorId, List<FilmSortField> sortBy) {
        Collection<Film> films = filmStorage.getByDirector(directorId, sortBy);

        return addFilmDetails(films);
    }

    private void checkUserExist(Long userId) {
        userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + userId + " не найден"));
    }

    private void checkFilmExist(Long filmId) {
        filmStorage.findById(filmId)
                .orElseThrow(() -> new NotFoundException("Фильм с id = " + filmId + " не найден"));
    }

    private Collection<Film> addFilmDetails(Collection<Film> films) {

        Set<Long> filmIds = films.stream().map(Film::getId).collect(Collectors.toSet());

        Map<Long, Set<Genre>> genresMap = genreStorage.getGenresForFilms(filmIds);
        Map<Long, Set<Director>> directorsMap = directorStorage.getDirectorsForFilms(filmIds);

        for (Film film : films) {
            film.setGenres(genresMap.getOrDefault(film.getId(), Set.of()));
            film.setDirectors(directorsMap.getOrDefault(film.getId(), Set.of()));
        }

        return films;
    }
}
