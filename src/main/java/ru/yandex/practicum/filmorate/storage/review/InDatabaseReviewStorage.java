package ru.yandex.practicum.filmorate.storage.review;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.InternalServerException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.utils.DatabaseUtils;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Collection;
import java.util.List;

@Component
public class InDatabaseReviewStorage implements ReviewStorage {

    private final JdbcTemplate jdbc;
    private final RowMapper<Review> mapper;

    private static final String GET_ITEMS = "select * from reviews ";
    private static final String FILM_FILTER = "where film_id = ? ";
    private static final String ORDER_FILTER = "order by useful desc ";
    private static final String LIMIT_FILTER = "limit ?";

    private static final String GET_ITEM = "select * from reviews where id = ?";
    private static final String INSERT_QUERY = "insert into reviews(film_id, user_id, content, is_positive) values (?, ?, ?, ?)";
    private static final String UPDATE_QUERY = "update reviews set content = ?, is_positive = ? where id = ?";
    private static final String REMOVE_QUERY = "delete from reviews where id = ?";
    private static final String REMOVE_QUERY_IN_REVIEW_USER = "delete from review_user where review_id = ?";

    private static final String UPDATE_USEFUL_QUERY = "update reviews set useful = (select sum(val) from review_user where review_id = ?) where id = ?";
    private static final String INSERT_REVIEW_USER_QUERY = "merge into review_user t " +
            "using ( " +
            "   select " +
            "   cast(? as number) as review_id, " +
            "   cast(? as number) as user_id, " +
            "   cast(? as number) as val " +
            ") as s " +
            "on t.user_id = s.user_id and t.review_id = s.review_id " +
            "when matched then update set val = s.val " +
            "when not matched then insert (review_id, user_id, val) " +
            "values (s.review_id, s.user_id, s.val)";
    private static final String DELETE_REVIEW_USER_QUERY = "delete from review_user where review_id = ? and user_id = ?";

    public InDatabaseReviewStorage(JdbcTemplate jdbc, RowMapper<Review> mapper) {
        this.jdbc = jdbc;
        this.mapper = mapper;
    }

    @Override
    public Review create(Review entity) {
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(INSERT_QUERY, Statement.RETURN_GENERATED_KEYS);
            ps.setObject(1, entity.getFilmId());
            ps.setObject(2, entity.getUserId());
            ps.setObject(3, entity.getContent());
            ps.setObject(4, entity.getIsPositive());
            return ps;
        }, keyHolder);
        Long id = keyHolder.getKeyAs(Long.class);
        if (id != null) {
            entity.setReviewId(id);
            DatabaseUtils.addDataToFeed(jdbc, entity.getUserId(), "REVIEW", "ADD", entity.getReviewId());
            return entity;
        } else {
            throw new InternalServerException("Не удалось сохранить данные");
        }
    }

    @Override
    public Review update(Review entity) {
        int rowsUpdated = jdbc.update(UPDATE_QUERY, entity.getContent(), entity.getIsPositive(), entity.getReviewId());
        if (rowsUpdated > 0) {
            DatabaseUtils.addDataToFeed(jdbc, entity.getUserId(), "REVIEW", "UPDATE", entity.getReviewId());
            return getItem(entity.getReviewId());
        } else {
            throw new NotFoundException("Отзыв с идентификатором " + entity.getReviewId() + " не существует");
        }
    }

    @Override
    public Collection<Review> getItems() {
        return jdbc.query(GET_ITEMS + ORDER_FILTER, mapper);
    }

    @Override
    public Collection<Review> getItems(Long filmId, Integer count) {
        if (filmId == null) {
            return jdbc.query(GET_ITEMS + ORDER_FILTER + LIMIT_FILTER, mapper, count);
        }
        return jdbc.query(GET_ITEMS + FILM_FILTER + ORDER_FILTER + LIMIT_FILTER, mapper, filmId, count);
    }

    @Override
    public Review getItem(Long id) {
        List<Long> checkVals = DatabaseUtils.getExistRows(jdbc, "reviews", List.of(id));
        if (checkVals.isEmpty()) {
            throw new NotFoundException("Не удалось найти отзыв по идентификатору");
        }
        return jdbc.queryForObject(GET_ITEM, mapper, id);
    }

    @Override
    public Boolean remove(Long reviewId) {
        List<Long> checkVals = DatabaseUtils.getExistRows(jdbc, "reviews", List.of(reviewId));
        if (checkVals.isEmpty()) {
            throw new NotFoundException("Не удалось найти отзыв по идентификатору");
        }
        Review review = getItem(reviewId);
        jdbc.update(REMOVE_QUERY_IN_REVIEW_USER, reviewId);
        int rowsUpdated = jdbc.update(REMOVE_QUERY, reviewId);
        DatabaseUtils.addDataToFeed(jdbc, review.getUserId(), "REVIEW", "REMOVE", review.getReviewId());
        return rowsUpdated > 0;
    }

    @Override
    public Review addLike(Long reviewId, Long userId) {
        jdbc.update(INSERT_REVIEW_USER_QUERY, reviewId, userId, 1);
        jdbc.update(UPDATE_USEFUL_QUERY, reviewId, reviewId);
        return getItem(reviewId);
    }

    @Override
    public Review removeLike(Long reviewId, Long userId) {
        jdbc.update(DELETE_REVIEW_USER_QUERY, reviewId, userId);
        jdbc.update(UPDATE_USEFUL_QUERY, reviewId, reviewId);
        return getItem(reviewId);
    }

    @Override
    public Review addDislike(Long reviewId, Long userId) {
        jdbc.update(INSERT_REVIEW_USER_QUERY, reviewId, userId, -1);
        jdbc.update(UPDATE_USEFUL_QUERY, reviewId, reviewId);
        return getItem(reviewId);
    }

    @Override
    public Review removeDislike(Long reviewId, Long userId) {
        jdbc.update(DELETE_REVIEW_USER_QUERY, reviewId, userId);
        jdbc.update(UPDATE_USEFUL_QUERY, reviewId, reviewId);
        return getItem(reviewId);
    }

}
