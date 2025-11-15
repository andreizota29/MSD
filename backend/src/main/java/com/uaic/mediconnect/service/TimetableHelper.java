package com.uaic.mediconnect.service;

import com.uaic.mediconnect.entity.TimetableTemplate;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.EnumSet;
import java.util.Set;

public class TimetableHelper {

    public static Set<DayOfWeek> getWorkingDays(TimetableTemplate timetableTemplate){
        return switch (timetableTemplate) {
            case WEEKDAY_9_18, WEEKDAY_8_17 -> EnumSet.range(DayOfWeek.MONDAY, DayOfWeek.FRIDAY);
            case WEEKEND_8_17, WEEKEND_9_18 -> EnumSet.range(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY);
            default -> throw new IllegalArgumentException("Unknown template");
        };
    }

    public static LocalTime getStartTime(TimetableTemplate timetableTemplate){
        return switch (timetableTemplate) {
            case WEEKDAY_8_17, WEEKEND_8_17 -> LocalTime.of(8, 0);
            case WEEKDAY_9_18, WEEKEND_9_18 -> LocalTime.of(9, 0);
            default -> throw new IllegalArgumentException("Unknown template");
        };
    }

    public static LocalTime getEndTime(TimetableTemplate timetableTemplate){
        return switch (timetableTemplate) {
            case WEEKDAY_8_17, WEEKEND_8_17 -> LocalTime.of(17, 0);
            case WEEKDAY_9_18, WEEKEND_9_18 -> LocalTime.of(18, 0);
            default -> throw new IllegalArgumentException("Unknown template");
        };
    }
}
