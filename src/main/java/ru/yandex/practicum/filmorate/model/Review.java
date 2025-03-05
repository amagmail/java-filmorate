package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Review {

    private Long reviewId;

    @NotNull
    private Long userId;

    @NotNull
    private Long filmId;

    @NotNull
    private String content;

    @NotNull
    private Boolean isPositive;
    private Integer useful = 0;

}
