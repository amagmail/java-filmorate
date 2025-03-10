package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class UserValidationTests {

    @Test
    public void userCreateTests() {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        User user = new User("USER", "USER@mail.ru");
        user.setBirthday(LocalDate.parse("1950-01-01", formatter));
        System.out.println(user);

        InMemoryUserStorage storage = new InMemoryUserStorage();
        InMemoryFilmStorage filmStorage = new InMemoryFilmStorage();
        UserService service = new UserService(storage, filmStorage);
        UserController controller = new UserController(service);

        User u1 = controller.create(user);
        System.out.println(u1);
        User u2 = controller.create(user);
        System.out.println(u2);

        LocalDate dateTo = LocalDate.now();
        Assertions.assertTrue(u1.getBirthday().isBefore(dateTo));
        Assertions.assertNotNull(u1.getLogin());
        Assertions.assertNotNull(u1.getEmail());
        Assertions.assertEquals(u1.getLogin(), u1.getName(), "Ошибка валидации");
        Assertions.assertEquals(controller.getItems().size(),  2, "Ошибка валидации");
    }

}
