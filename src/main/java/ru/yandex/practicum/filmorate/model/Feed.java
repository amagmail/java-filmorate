package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Feed {

    private Long eventId;
    private Long timestamp;
    private Long userId;
    private Long entityId;
    private String eventType;
    private String operation;

}
