package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.dao.jdbc.DirectorDbStorage;
import ru.yandex.practicum.filmorate.dao.jdbc.FilmLikesDbStorage;
import ru.yandex.practicum.filmorate.dao.jdbc.MpaDbStorage;
import ru.yandex.practicum.filmorate.dao.storage.FilmGenreStorage;
import ru.yandex.practicum.filmorate.dao.storage.FilmStorage;
import ru.yandex.practicum.filmorate.dao.storage.GenreStorage;
import ru.yandex.practicum.filmorate.dao.storage.UserStorage;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
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
    private final MpaDbStorage mpaDbStorage;
    private final FilmLikesDbStorage filmLikesDbStorage;
    private final EventService eventService;
    private final DirectorDbStorage directorDbStorage;

    @Autowired
    public FilmService(
            @Qualifier("filmDbStorage") FilmStorage filmStorage,
            @Qualifier("userDbStorage") UserStorage userStorage,
            @Qualifier("genreDbStorage") GenreStorage genreStorage,
            @Qualifier("filmGenreDbStorage") FilmGenreStorage filmGenreStorage,
            @Qualifier("mpaDbStorage") MpaDbStorage mpaDbStorage,
            @Qualifier("filmLikesDbStorage") FilmLikesDbStorage filmLikesDbStorage,
            @Qualifier("directorDbStorage") DirectorDbStorage directorDbStorage,
            EventService eventService) {

        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.genreStorage = genreStorage;
        this.filmGenreStorage = filmGenreStorage;
        this.mpaDbStorage = mpaDbStorage;
        this.filmLikesDbStorage = filmLikesDbStorage;
        this.directorDbStorage = directorDbStorage;
        this.eventService = eventService;
    }

    public Collection<Film> findAll() {
        Collection<Film> films = filmStorage.get();

        Set<Long> filmIds = films.stream()
                .map(Film::getId)
                .collect(Collectors.toSet());

        Map<Long, Set<Genre>> genresMap = genreStorage.getGenresForFilms(filmIds);

        for (Film film : films) {
            film.setGenres(genresMap.getOrDefault(film.getId(), Set.of()));
        }

        return films;
    }

    @Transactional
    public Film create(Film film) {
        if (film.getMpa() != null) {
            Mpa mpa = mpaDbStorage.getById(film.getMpa().getId())
                    .orElseThrow(() -> new NotFoundException("Рейтинг с id = " + film.getMpa().getId() + " не найден"));
            film.setMpa(mpa);
        }

        Set<Long> genreIds = film.getGenres().stream()
                .map(Genre::getId)
                .collect(Collectors.toSet());
        Collection<Genre> genres = genreStorage.getByIds(genreIds);

        if (genres.size() != genreIds.size()) {
            throw new NotFoundException("Один или несколько жанров не существуют");
        }

        Set<Long> directorsIds = film.getDirectors().stream()
                .map(Director::getId)
                .collect(Collectors.toSet());
        Collection<Director> directors = directorDbStorage.getByIds(directorsIds);

        film.setGenres(genres);
        film.setDirectors(directors);
        Film created = filmStorage.create(film);
        filmGenreStorage.saveGenresForFilm(created.getId(), created.getGenres());
        directorDbStorage.saveDirectorsForFilm(created.getId(), created.getDirectors());

        return findFilmById(created.getId());
    }

    @Transactional
    public Film update(Film film) {
        filmStorage.findById(film.getId())
                .orElseThrow(() -> new NotFoundException("Фильм с id = " + film.getId() + " не найден"));

        if (film.getMpa() != null) {
            Mpa mpa = mpaDbStorage.getById(film.getMpa().getId())
                    .orElseThrow(() -> new NotFoundException("Рейтинг с id = " + film.getMpa().getId() + " не найден"));
            film.setMpa(mpa);
        }

        Set<Long> genreIds = film.getGenres().stream()
                .map(Genre::getId)
                .collect(Collectors.toSet());
        Collection<Genre> genres = genreStorage.getByIds(genreIds);
        if (genres.size() != genreIds.size()) {
            throw new NotFoundException("Один или несколько жанров не существуют");
        }

        Set<Long> directorsIds = film.getDirectors().stream()
                .map(Director::getId)
                .collect(Collectors.toSet());
        Collection<Director> directors = directorDbStorage.getByIds(directorsIds);

        Film updatedFilm = filmStorage.update(film);
        filmGenreStorage.saveGenresForFilm(updatedFilm.getId(), genres);
        directorDbStorage.saveDirectorsForFilm(updatedFilm.getId(), directors);

        updatedFilm.setGenres(genres);
        updatedFilm.setDirectors(directors);

        return updatedFilm;
    }

    public Film findFilmById(Long id) {
        Film film = filmStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Фильм с id = " + id + " не найден"));

        Map<Long, Set<Genre>> genresMap = genreStorage.getGenresForFilms(Set.of(film.getId()));
        film.setGenres(genresMap.getOrDefault(film.getId(), Set.of()));

        return film;
    }

    public void addRate(Long filmId, Long userId) {
        Film film = filmStorage.findById(filmId)
                .orElseThrow(() -> new NotFoundException("Фильм с id = " + filmId + " не найден"));
        User user = userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + userId + " не найден"));

        filmLikesDbStorage.addLike(filmId, userId);

        eventService.createEvent(userId, Event.EventType.LIKE, Event.Operation.ADD, filmId);
    }

    public void removeRate(Long filmId, Long userId) {
        Film film = filmStorage.findById(filmId)
                .orElseThrow(() -> new NotFoundException("Фильм с id = " + filmId + " не найден"));
        User user = userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + userId + " не найден"));

        filmLikesDbStorage.removeLike(filmId, userId);

        eventService.createEvent(userId, Event.EventType.LIKE, Event.Operation.REMOVE, filmId);
    }

    public Collection<Film> getPopularFilms(int count) {
        Collection<Film> films = filmStorage.getPopular(count);

        Set<Long> filmIds = films.stream()
                .map(Film::getId)
                .collect(Collectors.toSet());

        Map<Long, Set<Genre>> genresMap = genreStorage.getGenresForFilms(filmIds);

        for (Film film : films) {
            film.setGenres(genresMap.getOrDefault(film.getId(), Set.of()));
        }

        return films;
    }

    public void deleteFilm(Long filmId) {
        filmStorage.deleteFilm(filmId);
    }

    private int getRatingCount(Film film) {
        return film.getRating() != null ? film.getRating().size() : 0;
    }

    public Collection<Film> findByDirector(Long directorId, List<FilmSortField> sortBy) {
        Collection<Film> films = filmStorage.getByDirector(directorId, sortBy);

        Set<Long> filmIds = films.stream()
                .map(Film::getId)
                .collect(Collectors.toSet());

        Map<Long, Set<Genre>> genresMap = genreStorage.getGenresForFilms(filmIds);
        Map<Long, Set<Director>> directorsMap = directorDbStorage.getDirectorsForFilms(filmIds);

        for (Film film : films) {
            film.setGenres(genresMap.getOrDefault(film.getId(), Set.of()));
            film.setDirectors(directorsMap.getOrDefault(film.getId(), Set.of()));
        }

        return films;
    }
}
