package com.example.medisense_ai.repository;

import com.example.medisense_ai.model.Appointment;
import com.example.medisense_ai.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByUser(User user);
    List<Appointment> findByUserOrderByAppointmentDateTimeAsc(User user);
    List<Appointment> findByUserAndAppointmentDateTimeAfterOrderByAppointmentDateTimeAsc(User user, LocalDateTime now);
    List<Appointment> findByUserAndStatusOrderByAppointmentDateTimeAsc(User user, Appointment.AppointmentStatus status);
    List<Appointment> findByUserAndAppointmentDateTimeBetweenOrderByAppointmentDateTimeAsc(User user, LocalDateTime start, LocalDateTime end);
}