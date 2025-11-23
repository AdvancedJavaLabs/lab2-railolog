package ru.ifmo.dto;

import lombok.Data;

@Data
public class TextTask {
    private String taskId;
    private String text;
    private String taskType;
    private int topN;
    private String nameReplacement;
}