package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.filmorate.annotation.AfterHistDate;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@EqualsAndHashCode(of = {"name", "releaseDate"})
public class Film {

    private Long id;
    private Set<Long> likes = new HashSet<>();
    private Set<Genre> genres = new HashSet<>();
    private Mpa mpa;

    @NotNull
    @NotBlank
    private String name;

    @Size(max = 200)
    private String description;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @AfterHistDate
    private LocalDate releaseDate;

    @Positive
    private Integer duration;

    public static int compareLikes(Film f1, Film f2) {
        return f2.likes.size() - f1.likes.size();
    }

    public Film(String name) {
        this.name = name;
    }

}
