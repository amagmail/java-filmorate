package ru.yandex.practicum.filmorate.annotation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.time.LocalDate;

public class AfterHistDateValidator implements ConstraintValidator<AfterHistDate, LocalDate> {

    private LocalDate dateFrom;

    @Override
    public void initialize(AfterHistDate constraintAnnotation) {
        dateFrom = LocalDate.of(1895, 12, 28);
    }

    @Override
    public boolean isValid(LocalDate releaseDate, ConstraintValidatorContext constraintValidatorContext) {
        return releaseDate == null || releaseDate.isEqual(LocalDate.from(dateFrom)) || releaseDate.isAfter(LocalDate.from(dateFrom));
    }

}
