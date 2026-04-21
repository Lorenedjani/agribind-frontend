package com.agribind.communication.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Date;

/**
 * Utility class for date and time operations
 */
@Component
public class DateTimeUtil {

    private static final Logger log = LoggerFactory.getLogger(DateTimeUtil.class);

    // Cameroon timezone
    private static final ZoneId CAMEROON_ZONE = ZoneId.of("Africa/Douala");

    // Common date formats
    private static final DateTimeFormatter ISO_DATE_TIME = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private static final DateTimeFormatter DISPLAY_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter DATE_ONLY = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME_ONLY = DateTimeFormatter.ofPattern("HH:mm");

    /**
     * Get current date and time in Cameroon timezone
     *
     * @return Current LocalDateTime
     */
    public LocalDateTime getCurrentDateTime() {
        return LocalDateTime.now(CAMEROON_ZONE);
    }

    /**
     * Get current date in Cameroon timezone
     *
     * @return Current LocalDate
     */
    public LocalDate getCurrentDate() {
        return LocalDate.now(CAMEROON_ZONE);
    }

    /**
     * Format LocalDateTime for display
     *
     * @param dateTime DateTime to format
     * @return Formatted string (dd/MM/yyyy HH:mm)
     */
    public String formatForDisplay(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.format(DISPLAY_FORMAT);
    }

    /**
     * Format LocalDate for display
     *
     * @param date Date to format
     * @return Formatted string (dd/MM/yyyy)
     */
    public String formatDateForDisplay(LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.format(DATE_ONLY);
    }

    /**
     * Format time only
     *
     * @param dateTime DateTime to format
     * @return Formatted time string (HH:mm)
     */
    public String formatTimeOnly(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.format(TIME_ONLY);
    }

    /**
     * Parse date string in ISO format
     *
     * @param dateString Date string
     * @return LocalDateTime
     */
    public LocalDateTime parseIsoDateTime(String dateString) {
        if (dateString == null || dateString.isEmpty()) {
            return null;
        }
        try {
            return LocalDateTime.parse(dateString, ISO_DATE_TIME);
        } catch (Exception e) {
            log.error("Error parsing date string: {}", dateString, e);
            return null;
        }
    }

    /**
     * Convert Date to LocalDateTime
     *
     * @param date Date object
     * @return LocalDateTime
     */
    public LocalDateTime toLocalDateTime(Date date) {
        if (date == null) {
            return null;
        }
        return date.toInstant()
                .atZone(CAMEROON_ZONE)
                .toLocalDateTime();
    }

    /**
     * Convert LocalDateTime to Date
     *
     * @param localDateTime LocalDateTime
     * @return Date object
     */
    public Date toDate(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        return Date.from(localDateTime.atZone(CAMEROON_ZONE).toInstant());
    }

    /**
     * Get start of day for given date
     *
     * @param date Date
     * @return LocalDateTime at start of day (00:00:00)
     */
    public LocalDateTime getStartOfDay(LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.atStartOfDay();
    }

    /**
     * Get end of day for given date
     *
     * @param date Date
     * @return LocalDateTime at end of day (23:59:59)
     */
    public LocalDateTime getEndOfDay(LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.atTime(23, 59, 59);
    }

    /**
     * Calculate difference between two dates in days
     *
     * @param start Start date
     * @param end End date
     * @return Number of days
     */
    public long getDaysBetween(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            return 0;
        }
        return ChronoUnit.DAYS.between(start, end);
    }

    /**
     * Calculate difference between two dates in hours
     *
     * @param start Start date
     * @param end End date
     * @return Number of hours
     */
    public long getHoursBetween(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            return 0;
        }
        return ChronoUnit.HOURS.between(start, end);
    }

    /**
     * Calculate difference between two dates in minutes
     *
     * @param start Start date
     * @param end End date
     * @return Number of minutes
     */
    public long getMinutesBetween(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            return 0;
        }
        return ChronoUnit.MINUTES.between(start, end);
    }

    /**
     * Format duration as human-readable string
     *
     * @param start Start date
     * @param end End date
     * @return Human-readable duration (e.g., "2 hours ago", "3 days ago")
     */
    public String formatDuration(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            return "Unknown";
        }

        Duration duration = Duration.between(start, end);
        long seconds = duration.getSeconds();

        if (seconds < 60) {
            return "Just now";
        } else if (seconds < 3600) {
            long minutes = seconds / 60;
            return minutes + (minutes == 1 ? " minute ago" : " minutes ago");
        } else if (seconds < 86400) {
            long hours = seconds / 3600;
            return hours + (hours == 1 ? " hour ago" : " hours ago");
        } else if (seconds < 604800) {
            long days = seconds / 86400;
            return days + (days == 1 ? " day ago" : " days ago");
        } else if (seconds < 2592000) {
            long weeks = seconds / 604800;
            return weeks + (weeks == 1 ? " week ago" : " weeks ago");
        } else {
            long months = seconds / 2592000;
            return months + (months == 1 ? " month ago" : " months ago");
        }
    }

    /**
     * Get relative time string from now
     *
     * @param dateTime DateTime to compare
     * @return Relative time string
     */
    public String getRelativeTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "Unknown";
        }
        return formatDuration(dateTime, getCurrentDateTime());
    }

    /**
     * Check if date is today
     *
     * @param date Date to check
     * @return true if today, false otherwise
     */
    public boolean isToday(LocalDateTime date) {
        if (date == null) {
            return false;
        }
        return date.toLocalDate().isEqual(getCurrentDate());
    }

    /**
     * Check if date is in current week
     *
     * @param date Date to check
     * @return true if in current week, false otherwise
     */
    public boolean isThisWeek(LocalDateTime date) {
        if (date == null) {
            return false;
        }

        LocalDate now = getCurrentDate();
        LocalDate startOfWeek = now.minusDays(now.getDayOfWeek().getValue() - 1);
        LocalDate endOfWeek = startOfWeek.plusDays(6);

        LocalDate checkDate = date.toLocalDate();
        return !checkDate.isBefore(startOfWeek) && !checkDate.isAfter(endOfWeek);
    }

    /**
     * Check if date is in current month
     *
     * @param date Date to check
     * @return true if in current month, false otherwise
     */
    public boolean isThisMonth(LocalDateTime date) {
        if (date == null) {
            return false;
        }

        LocalDate now = getCurrentDate();
        LocalDate checkDate = date.toLocalDate();

        return checkDate.getYear() == now.getYear() &&
               checkDate.getMonth() == now.getMonth();
    }

    /**
     * Get start of current week
     *
     * @return LocalDateTime at start of week
     */
    public LocalDateTime getStartOfWeek() {
        LocalDate now = getCurrentDate();
        LocalDate startOfWeek = now.minusDays(now.getDayOfWeek().getValue() - 1);
        return startOfWeek.atStartOfDay();
    }

    /**
     * Get start of current month
     *
     * @return LocalDateTime at start of month
     */
    public LocalDateTime getStartOfMonth() {
        LocalDate now = getCurrentDate();
        return now.withDayOfMonth(1).atStartOfDay();
    }

    /**
     * Get start of current year
     *
     * @return LocalDateTime at start of year
     */
    public LocalDateTime getStartOfYear() {
        LocalDate now = getCurrentDate();
        return now.withDayOfYear(1).atStartOfDay();
    }

    /**
     * Add days to date
     *
     * @param date Base date
     * @param days Number of days to add
     * @return New LocalDateTime
     */
    public LocalDateTime addDays(LocalDateTime date, long days) {
        if (date == null) {
            return null;
        }
        return date.plusDays(days);
    }

    /**
     * Add hours to date
     *
     * @param date Base date
     * @param hours Number of hours to add
     * @return New LocalDateTime
     */
    public LocalDateTime addHours(LocalDateTime date, long hours) {
        if (date == null) {
            return null;
        }
        return date.plusHours(hours);
    }

    /**
     * Check if date is in the past
     *
     * @param date Date to check
     * @return true if in past, false otherwise
     */
    public boolean isInPast(LocalDateTime date) {
        if (date == null) {
            return false;
        }
        return date.isBefore(getCurrentDateTime());
    }

    /**
     * Check if date is in the future
     *
     * @param date Date to check
     * @return true if in future, false otherwise
     */
    public boolean isInFuture(LocalDateTime date) {
        if (date == null) {
            return false;
        }
        return date.isAfter(getCurrentDateTime());
    }
}