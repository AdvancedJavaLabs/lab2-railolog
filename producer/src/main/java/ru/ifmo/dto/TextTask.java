package ru.ifmo.dto;

import lombok.Data;

@Data
public class TextTask {
    private String taskId;
    private String text;
    private int topN; // for top-N words task, default will be handled in service
    private String nameReplacement; // for name replacement task, default will be handled in service
}