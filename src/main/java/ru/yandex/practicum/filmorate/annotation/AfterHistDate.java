package ru.yandex.practicum.filmorate.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = AfterHistDateValidator.class)
public @interface AfterHistDate {

    String message() default "Date value out of historical range";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

}

