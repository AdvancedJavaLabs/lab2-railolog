package ru.ifmo.dto;

import lombok.Data;

@Data
public class SessionInfo {
    private String sessionId;
    private int expectedTaskCount;
    private String description;
}