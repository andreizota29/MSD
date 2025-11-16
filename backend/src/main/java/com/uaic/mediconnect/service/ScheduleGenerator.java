package com.uaic.mediconnect.service;

import com.uaic.mediconnect.entity.Doctor;
import com.uaic.mediconnect.entity.DoctorSchedule;
import com.uaic.mediconnect.entity.TimetableTemplate;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Service
public class ScheduleGenerator {

    public List<DoctorSchedule> generate90Days(Doctor doctor){

        List<DoctorSchedule> list = new ArrayList<>();
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(90);

        Set<DayOfWeek> workingDays = getWorkingDays(doctor.getTimetableTemplate());
        LocalTime start = getStartTime(doctor.getTimetableTemplate());
        LocalTime end = getEndTime(doctor.getTimetableTemplate());

        for (LocalDate d = startDate; !d.isAfter(endDate); d = d.plusDays(1)) {

            if (!workingDays.contains(d.getDayOfWeek()))
                continue;

            LocalTime time = start;

            while (time.isBefore(end)) {
                DoctorSchedule slot = new DoctorSchedule();
                slot.setDoctor(doctor);
                slot.setDate(d);
                slot.setStartTime(time);
                slot.setEndTime(time.plusMinutes(30));
                slot.setBooked(false);
                slot.setPatient(null);

                list.add(slot);

                time = time.plusMinutes(30);
            }
        }

        return list;
    }

    private Set<DayOfWeek> getWorkingDays(TimetableTemplate t) {
        return switch (t) {
            case WEEKDAY_9_18, WEEKDAY_8_17 ->
                    EnumSet.range(DayOfWeek.MONDAY, DayOfWeek.FRIDAY);
            case WEEKEND_9_18, WEEKEND_8_17 ->
                    EnumSet.of(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY);
        };
    }

    private LocalTime getStartTime(TimetableTemplate t) {
        return switch (t) {
            case WEEKDAY_8_17, WEEKEND_8_17 -> LocalTime.of(8, 0);
            case WEEKDAY_9_18, WEEKEND_9_18 -> LocalTime.of(9, 0);
        };
    }

    private LocalTime getEndTime(TimetableTemplate t) {
        return switch (t) {
            case WEEKDAY_8_17, WEEKEND_8_17 -> LocalTime.of(17, 0);
            case WEEKDAY_9_18, WEEKEND_9_18 -> LocalTime.of(18, 0);
        };
    }
}
