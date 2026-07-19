package ru.yandex.practicum.filmorate.model;

import lombok.Data;

@Data
public class UserFriendship {

    private Long userId;

    private Long friendId;

    private Status status;

    public enum Status {
        PENDING,
        CONFIRMED
    }
}