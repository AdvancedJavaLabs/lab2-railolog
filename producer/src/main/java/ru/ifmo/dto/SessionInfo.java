package ru.ifmo.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class SessionInfo {
    private String sessionId;
    private int expectedTaskCount;
    private String description;
    private LocalDateTime startTime;
}