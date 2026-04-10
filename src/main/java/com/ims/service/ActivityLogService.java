package com.ims.service;

import com.ims.dao.ActivityLogDAO;
import com.ims.model.ActivityLogEntry;
import com.ims.model.User;

import java.time.LocalDateTime;
import java.util.List;

public class ActivityLogService {
    private final ActivityLogDAO activityLogDAO = new ActivityLogDAO();

    public void log(User user, String actionType, String subject, String details) {
        String actor = user == null || user.getUsername() == null || user.getUsername().isBlank()
            ? "System"
            : user.getUsername().trim();
        activityLogDAO.save(
            normalizedText(actionType, "Updated"),
            normalizedText(subject, "Record"),
            details == null ? "" : details.trim(),
            actor,
            LocalDateTime.now()
        );
    }

    public List<ActivityLogEntry> recentActivity(int limit) {
        return activityLogDAO.findRecent(limit);
    }

    private String normalizedText(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }
}
