package com.example.medisense_ai.service;

import com.example.medisense_ai.model.Appointment;
import com.example.medisense_ai.model.User;
import com.example.medisense_ai.repository.AppointmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;

    @Autowired
    public AppointmentService(AppointmentRepository appointmentRepository) {
        this.appointmentRepository = appointmentRepository;
    }

    public Appointment saveAppointment(Appointment appointment) {
        return appointmentRepository.save(appointment);
    }

    public Optional<Appointment> findById(Long id) {
        return appointmentRepository.findById(id);
    }

    public List<Appointment> findAllByUser(User user) {
        return appointmentRepository.findByUser(user);
    }

    public List<Appointment> findUpcomingAppointments(User user) {
        return appointmentRepository.findByUserAndAppointmentDateTimeAfterOrderByAppointmentDateTimeAsc(
                user, LocalDateTime.now());
    }

    public List<Appointment> findAppointmentsByStatus(User user, Appointment.AppointmentStatus status) {
        return appointmentRepository.findByUserAndStatusOrderByAppointmentDateTimeAsc(user, status);
    }

    public List<Appointment> findAppointmentsByDateRange(User user, LocalDateTime start, LocalDateTime end) {
        return appointmentRepository.findByUserAndAppointmentDateTimeBetweenOrderByAppointmentDateTimeAsc(
                user, start, end);
    }

    public void deleteAppointment(Long id) {
        appointmentRepository.deleteById(id);
    }

    public Appointment updateAppointmentStatus(Long id, Appointment.AppointmentStatus status) {
        Optional<Appointment> appointmentOpt = appointmentRepository.findById(id);
        if (appointmentOpt.isPresent()) {
            Appointment appointment = appointmentOpt.get();
            appointment.setStatus(status);
            return appointmentRepository.save(appointment);
        }
        return null;
    }
}