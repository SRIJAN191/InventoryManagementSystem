package com.ims.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public record ActivityLogEntry(
    int logId,
    String actionType,
    String subject,
    String details,
    String performedBy,
    LocalDateTime performedAt
) {
    private static final DateTimeFormatter DISPLAY_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public String formattedTimestamp() {
        return performedAt == null ? "-" : performedAt.format(DISPLAY_FORMATTER);
    }

    public String summary() {
        String actor = performedBy == null || performedBy.isBlank() ? "System" : performedBy;
        String action = actionType == null || actionType.isBlank() ? "Updated" : actionType;
        String target = subject == null || subject.isBlank() ? "record" : subject;
        String note = details == null || details.isBlank() ? "" : " - " + details;
        return formattedTimestamp() + " | " + actor + " | " + action + " | " + target + note;
    }
}
