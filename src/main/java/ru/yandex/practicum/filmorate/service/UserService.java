package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.storage.FilmLikesStorage;
import ru.yandex.practicum.filmorate.dao.storage.FilmStorage;
import ru.yandex.practicum.filmorate.dao.storage.UserStorage;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.FilmLike;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserService {
    private final UserStorage userStorage;
    private final EventService eventService;
    private final FilmLikesStorage filmLikesStorage;
    private final FilmStorage filmStorage;

    @Autowired
    public UserService(
            @Qualifier("userDbStorage") UserStorage userStorage,
            EventService eventService,
            FilmLikesStorage filmLikesStorage,
            @Qualifier("filmDbStorage") FilmStorage filmStorage) {

        this.userStorage = userStorage;
        this.eventService = eventService;
        this.filmLikesStorage = filmLikesStorage;
        this.filmStorage = filmStorage;
    }

    private static Long getSimilarUserId(Map<Long, Double> cosineSimilarity) {
        log.info("Поиск похожего пользователя");
        Long alterUserId = null;
        //выбираем максимальный коэффициент сходства
        Double maxCosine = cosineSimilarity.values().stream().max(Double::compareTo).orElse(0.0);
        //находим пользователя с этим максимальным коэффициентом - по условиям задачи берем только одного
        if (maxCosine > 0.0) {  //берем только с ненулевым коэффициентом
            Optional<Map.Entry<Long, Double>> alterUserIdOpt = cosineSimilarity.entrySet()
                    .stream()
                    .filter(e -> Objects.equals(e.getValue(), maxCosine))
                    .findFirst();
            if (alterUserIdOpt.isPresent()) {
                alterUserId = alterUserIdOpt.get().getKey();
            }
        }
        return alterUserId;
    }

    private static Map<Long, Set<Long>> getLikesMap(Collection<FilmLike> likes) {
        log.info("Получение мапы лайков");
        Map<Long, Set<Long>> likesMap = new HashMap<>();
        likes.forEach(like -> {
            Set<Long> filmIds = likesMap.computeIfAbsent(like.getUserId(), k -> new HashSet<>());
            filmIds.add(like.getFilmId());
        });
        return likesMap;
    }

    private static Map<Long, HashMap<Long, Integer>> getRates(Map<Long, Set<Long>> likesMap, Set<Long> films) {
        log.info("Получение матрицы оценок");
        Map<Long, HashMap<Long, Integer>> rates = new HashMap<>();
        likesMap.forEach((k, v) -> {
            HashMap<Long, Integer> filmRates = rates.computeIfAbsent(k, k1 -> new HashMap<>());
            //записываем фильм с лайком с коэффициентом 1
            v.forEach(filmId -> filmRates.put(filmId, 1));
            //записываем фильм без лайка с коэффициентом 0
            films.stream()
                    .filter(filmId -> filmRates.get(filmId) == null)
                    .forEach(filmId -> filmRates.put(filmId, 0));
        });
        return rates;
    }

    private static Map<Long, Double> getCosineSimilarity(Map<Long, HashMap<Long, Integer>> rates,
                                                         HashMap<Long, Integer> ourUserRates,
                                                         double ourUserLength) {
        log.info("Расчет косинусного сходства");
        Map<Long, Double> cosineSimilarity = new HashMap<>();
        rates.forEach((k, v) -> {
            double cosine = 0;
            int scale = 0;
            //считаем скалярное произведение векторов
            for (Map.Entry<Long, Integer> rate : v.entrySet()) {
                //перемножаем оценки по каждому фильму у нашего пользователя и того пользователя, которого обрабатываем
                int pair = rate.getValue() * ourUserRates.get(rate.getKey());
                scale = scale + pair;
            }
            //считаем длину вектора этого пользователя
            double length = calcLength(v.values());
            //если длины не нули, то считаем косинусное сходство
            if (ourUserLength != 0 && length != 0) {
                cosine = scale / (length * ourUserLength);
            }
            cosineSimilarity.put(k, cosine);
        });
        return cosineSimilarity;
    }

    private static double calcLength(Collection<Integer> rates) {
        log.info("Расчет длины векторов (эвклидовы нормы)");
        double ourUserLength = 0.0;
        for (Integer rate : rates) {
            ourUserLength = ourUserLength + (rate * rate);
        }
        ourUserLength = Math.sqrt(ourUserLength);
        return ourUserLength;
    }

    public Collection<User> findAll() {
        return userStorage.get();
    }

    public User create(User user) {
        return userStorage.create(user);
    }

    public User update(User user) {
        findUserById(user.getId());
        return userStorage.update(user);
    }

    public User findUserById(Long id) {
        return userStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + id + " не найден"));
    }

    public void addFriend(Long userId, Long friendId) {
        User user = findUserById(userId);
        User friend = findUserById(friendId);

        log.info("Отправляем запрос от userId = {} на добавление в друзья friendId = {}", userId, friendId);
        userStorage.addFriend(user.getId(), friend.getId());

        eventService.createEvent(userId, Event.EventType.FRIEND, Event.Operation.ADD, friendId);
    }

    public void removeFriend(Long userId, Long friendId) {
        User user = findUserById(userId);
        User friend = findUserById(friendId);

        log.info("Удаление заявки userId = {} на добавление в друзья friendId = {}", userId, friendId);
        userStorage.removeFriend(user.getId(), friend.getId());

        eventService.createEvent(userId, Event.EventType.FRIEND, Event.Operation.REMOVE, friendId);
    }

    public Collection<User> getFriends(Long userId) {
        User user = findUserById(userId);

        return userStorage.getFriends(user.getId());
    }

    public Collection<User> getCommonFriends(Long userId, Long otherUserId) {
        User user = findUserById(userId);
        User otherUser = findUserById(otherUserId);

        return userStorage.getCommonFriends(user.getId(), otherUser.getId());
    }

    public void deleteUser(Long userId) {
        userStorage.deleteUser(userId);
        log.info("Удаление пользователя с id = {}", userId);
    }

    public Collection<Film> getRecommendations(Long userId) {
        log.info("Поиск рекомендация по фильмам для userId = {} ", userId);
        Collection<Film> recommendedFilms = new ArrayList<>();
        Collection<FilmLike> likes = filmLikesStorage.get();
        if (likes.isEmpty()) {  //нет лайков - сразу уходим
            return recommendedFilms;
        }
        //забираем уникальный набор пользователей - в списках лайках может быть дублирование
        Set<Long> users = likes.stream().map(FilmLike::getUserId).collect(Collectors.toSet());
        if (!users.contains(userId) || users.size() == 1) { //наш клиент ничего не лайкал или лайкал только он - на выход
            return recommendedFilms;
        }
        //забираем уникальный набор фильмов - в списках лайках может быть дублирование
        Set<Long> films = likes.stream().map(FilmLike::getFilmId).collect(Collectors.toSet());
        //набираем мапу существующих лайков
        Map<Long, Set<Long>> likesMap = getLikesMap(likes);
        //набираем матрицу все пользователи-все фильмы-оценки
        Map<Long, HashMap<Long, Integer>> rates = getRates(likesMap, films);
        //Забираем данные нашего пользователя и убираем из общего расчета
        HashMap<Long, Integer> ourUserRates = rates.get(userId);
        rates.remove(userId);
        //посчитаем для него сразу длину (евклидову норму)
        double ourUserLength = calcLength(ourUserRates.values());
        //считаем косинусное сходство для каждого пользователя в сравнении с нашим пользователем
        Map<Long, Double> cosineSimilarity = getCosineSimilarity(rates, ourUserRates, ourUserLength);
        //ищем самого похожего пользователя
        Long alterUserId = getSimilarUserId(cosineSimilarity);
        if (alterUserId != null) {  //есть такой пользователь
            //набираем id фильмов которые ЕСТЬ у второго пользователя и НЕТ у нашего пользователя
            Set<Long> notMatchedIds = likesMap.get(alterUserId)
                    .stream()
                    .filter(id -> !likesMap.get(userId).contains(id))
                    .collect(Collectors.toSet());
            if (!notMatchedIds.isEmpty()) {  //есть хоть что-то что можем рекомендовать
                recommendedFilms = filmStorage.getByIds(notMatchedIds);
            }
        }
        return recommendedFilms;
    }
}
