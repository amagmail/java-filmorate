**Описание сервиса FILMORATE**
- Работает с фильмами и оценками пользователей
- Возвращать топ-5 фильмов, рекомендованных к просмотру
- Возможность составлять рейтинги фильмов
- Возможность оставлять отзывы пользователей
- Сохранение состояния данных после перезапуска
- 15

**Схема БД**
![Filmorate database scheme](/db_scheme.png)

**Описание схемы:**
1. USERS --> PK = id
   * id- Уникальный идентификатор
   * name - Имя пользователя
   * email - Электронная почта
   * login - Логин пользователя
   * birthday - Дата рождения
2. FRIENDSHIP --> PK = (user_id, friend_id)
   * user_id - Идентификатор пользователя 
   * friend_id - Идентификатор друга
   * accepted - Дружба подтверждена
3. FILMS --> PK = id
   * id - Уникальный идентификатор
   * name - Название фильма
   * description - Описание фильма
   * release_date - Дата выхода
   * duration - Длительность в минутах
   * mpa - Рейтинг ассоциации MPA
4. LIKES --> PK = (user_id, film_id)
   * user_id - Идентификатор пользователя
   * film_id - Идентификатор фильма 
5. FILM_GENRE --> PK = (film_id, genre_id)
   * film_id - Идентификатор фильма 
   * genre_id - Идентификатор жанра
6. GENRES --> PK = id
   * id - Уникальный идентификатор 
   * name - Название жанра
7. MPA --> PK = id
   * id - Уникальный идентификатор
   * name - Код рейтинга
   * description - Описание рейтинга