package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class FilmValidationTests {

    @Test
    public void filmCreateTests() {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        Film film = new Film("TEXT");
        film.setDescription("TEXT");
        film.setReleaseDate(LocalDate.parse("1995-12-28", formatter));
        film.setDuration(100);
        System.out.println(film);

        InMemoryFilmStorage filmStorage = new InMemoryFilmStorage();
        InMemoryUserStorage userStorage = new InMemoryUserStorage();
        FilmService service = new FilmService(filmStorage, userStorage);
        FilmController controller = new FilmController(service);

        Film f1 = controller.create(film);
        System.out.println(f1);
        Film f2 = controller.create(film);
        System.out.println(f2);

        LocalDate dateFrom = LocalDate.of(1895, 12, 28);
        Assertions.assertFalse(f1.getReleaseDate().isBefore(LocalDate.from(dateFrom)));
        Assertions.assertTrue(f1.getDuration() > 0);
        Assertions.assertNotNull(f1.getName());
        Assertions.assertEquals(controller.getItems().size(),  2, "Ошибка валидации");
    }

}
