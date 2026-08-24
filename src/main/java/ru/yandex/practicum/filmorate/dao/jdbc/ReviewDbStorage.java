package ru.yandex.practicum.filmorate.dao.jdbc;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dao.storage.ReviewStorage;
import ru.yandex.practicum.filmorate.model.Review;

import java.util.Collection;
import java.util.Optional;

@Repository
public class ReviewDbStorage extends BaseDbStorage<Review> implements ReviewStorage {

    private static final String INSERT_QUERY = "INSERT INTO reviews(content, is_positive, user_id, film_id, useful) " +
            "VALUES (?, ?, ?, ?, ?)";
    private static final String UPDATE_QUERY = "UPDATE reviews " +
            "SET content = ?, is_positive = ? " +
            "WHERE id = ?";
    private static final String DELETE_QUERY = "DELETE FROM reviews " +
            "WHERE id = ?";
    private static final String FIND_BY_ID_QUERY = "SELECT id, content, is_positive, user_id, film_id, useful " +
            "FROM reviews WHERE id = ?";
    private static final String GET_BY_FILM_QUERY = "SELECT id, content, is_positive, user_id, film_id, useful " +
            "FROM reviews " +
            "WHERE film_id = ? " +
            "ORDER BY useful DESC, id " +
            "LIMIT ? ";
    private static final String GET_BY_COUNT_QUERY = """
            SELECT  id,
                    content,
                    is_positive,
                    user_id,
                    film_id,
                    useful
            FROM reviews
            ORDER BY useful DESC, id
            LIMIT ?
            """;
    private static final String ADD_LIKE_QUERY = "INSERT INTO reviews_reaction(review_id, user_id, type_reaction) " +
            "VALUES (?, ?, 1)";
    private static final String UPDATE_USEFUL_PLUS_QUERY = "UPDATE reviews " +
            "SET useful = useful + 1 " +
            "WHERE id = ?";
    private static final String UPDATE_USEFUL_MINUS_QUERY = "UPDATE reviews " +
            "SET useful = useful - 1 " +
            "WHERE id = ?";
    private static final String ADD_DISLIKE_QUERY = "INSERT INTO reviews_reaction(review_id, user_id, type_reaction) " +
            "VALUES (?, ?, 2)";
    private static final String REMOVE_LIKE_QUERY = "DELETE FROM reviews_reaction " +
            "WHERE review_id = ? AND user_id = ? AND type_reaction = 1";
    private static final String REMOVE_DISLIKE_QUERY = "DELETE FROM reviews_reaction " +
            "WHERE review_id = ? AND user_id = ? AND type_reaction = 2";
    private static final String COUNT_LIKE_QUERY = "SELECT COUNT(*) AS COUNT_LIKES " +
                                                   "FROM reviews_reaction " +
                                                   "WHERE review_id = ? AND user_id = ? AND type_reaction = 1";
    private static final String COUNT_DISLIKE_QUERY = "SELECT COUNT(*) AS COUNT_LIKES " +
                                                      "FROM reviews_reaction " +
                                                      "WHERE review_id = ? AND user_id = ? AND type_reaction = 2";

    protected ReviewDbStorage(JdbcTemplate jdbc, RowMapper<Review> mapper) {
        super(jdbc, mapper);
    }

    @Override
    public Review create(Review review) {
        long id = insert(
                INSERT_QUERY,
                review.getContent(),
                review.getIsPositive(),
                review.getUserId(),
                review.getFilmId(),
                0
        );
        review.setReviewId(id);
        return review;
    }

    @Override
    public Review update(Review review) {
        update(
                UPDATE_QUERY,
                review.getContent(),
                review.getIsPositive(),
                review.getReviewId()
        );
        //так как useful мы не апдейтим напрямую, то достаем актуальный из базы
        Optional<Review> optional = findById(review.getReviewId());
        return optional.orElse(review);
    }

    @Override
    public void delete(Long reviewId) {
        execute(DELETE_QUERY, reviewId);
    }

    @Override
    public Optional<Review> findById(Long reviewId) {
        return findOne(FIND_BY_ID_QUERY, reviewId);
    }

    @Override
    public Collection<Review> getByCount(int count) {
        return findMany(GET_BY_COUNT_QUERY, count);
    }

    @Override
    public Collection<Review> getByFilmAndCount(Long filmId, int count) {
        return findMany(GET_BY_FILM_QUERY, filmId, count);
    }

    @Override
    public void addLike(Long reviewId, Long userId) {
        Integer like = count(COUNT_LIKE_QUERY, reviewId, userId);
        if (like == 0) { //повторно не ставим лайк
            execute(ADD_LIKE_QUERY, reviewId, userId);
            update(UPDATE_USEFUL_PLUS_QUERY, reviewId);
        }
        Integer dislike = count(COUNT_DISLIKE_QUERY, reviewId, userId);
        if (dislike != 0) {  //если был дислайк - снимаем его и выправляем рейтинг
            execute(REMOVE_DISLIKE_QUERY, reviewId, userId);
            update(UPDATE_USEFUL_PLUS_QUERY, reviewId);
        }
    }

    @Override
    public void addDisLike(Long reviewId, Long userId) {
        Integer dislike = count(COUNT_DISLIKE_QUERY, reviewId, userId);
        if (dislike == 0) { //повторно не ставим дизлайк
            execute(ADD_DISLIKE_QUERY, reviewId, userId);
            update(UPDATE_USEFUL_MINUS_QUERY, reviewId);
        }
        Integer like = count(COUNT_LIKE_QUERY, reviewId, userId);
        if (like != 0) { //если был лайк - снимаем его и выправляем рейтинг
            execute(REMOVE_LIKE_QUERY, reviewId, userId);
            update(UPDATE_USEFUL_MINUS_QUERY, reviewId);
        }
    }

    @Override
    public void removeLike(Long reviewId, Long userId) {
        execute(REMOVE_LIKE_QUERY, reviewId, userId);
        update(UPDATE_USEFUL_MINUS_QUERY, reviewId);
    }

    @Override
    public void removeDisLike(Long reviewId, Long userId) {
        execute(REMOVE_DISLIKE_QUERY, reviewId, userId);
        update(UPDATE_USEFUL_PLUS_QUERY, reviewId);
    }
}
