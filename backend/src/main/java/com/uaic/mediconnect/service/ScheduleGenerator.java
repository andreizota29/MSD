package com.uaic.mediconnect.service;

import com.uaic.mediconnect.entity.Doctor;
import com.uaic.mediconnect.entity.DoctorSchedule;
import com.uaic.mediconnect.entity.TimetableTemplate;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ScheduleGenerator {

    public static List<DoctorSchedule> generateSchedule(Doctor doctor, TimetableTemplate timetableTemplate){
        List<DoctorSchedule> scheduleList = new ArrayList<>();

        LocalDate today = LocalDate.now();
        LocalDate endDate = today.plusMonths(2);

        Set<DayOfWeek> workingDays = TimetableHelper.getWorkingDays(timetableTemplate);
        LocalTime start = TimetableHelper.getStartTime(timetableTemplate);
        LocalTime end = TimetableHelper.getEndTime(timetableTemplate);

        for(LocalDate date = today; !date.isAfter(endDate); date = date.plusDays(1)){
            if(!workingDays.contains(date.getDayOfWeek())) continue;
            LocalTime time = start;
            while(time.isBefore(end)) {
                DoctorSchedule slot = new DoctorSchedule();
                slot.setDoctor(doctor);
                slot.setDate(date);
                slot.setStartTime(time);
                slot.setEndTime(time.plusMinutes(30));
                slot.setBooked(false);
                slot.setPatient(null);
                scheduleList.add(slot);
                time = time.plusMinutes(30);
            }

        }
        return  scheduleList;
    }
}
