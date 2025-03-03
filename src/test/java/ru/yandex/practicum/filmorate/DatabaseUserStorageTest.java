package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.InDatabaseUserStorage;
import ru.yandex.practicum.filmorate.storage.user.mappers.UserRowMapper;

import java.time.LocalDate;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({InDatabaseUserStorage.class, UserRowMapper.class})
public class DatabaseUserStorageTest {

    private final InDatabaseUserStorage userStorage;

    @Test
    public void test() {

        System.out.println("Begin");
        User user = new User();
        user.setLogin("test");
        user.setEmail("test@mail.ru");
        user.setName("test");
        user.setBirthday(LocalDate.of(2000, 1, 1));

        User userRes = userStorage.create(user);
        Assertions.assertNotNull(userRes.getId());
        System.out.println(userStorage.getItems());
        System.out.println("End");
    }

}