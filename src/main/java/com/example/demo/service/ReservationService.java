package com.example.demo.service;

import com.example.demo.entity.HotelRoom;
import com.example.demo.entity.Reservation;
import com.example.demo.repository.HotelRoomRepository;
import com.example.demo.repository.ReservationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class ReservationService {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private HotelRoomRepository hotelRoomRepository;

    public List<Reservation> getAllReservations() {
        return reservationRepository.findAllWithRoom();  // ✅ Use the JOIN FETCH version
    }

    public Optional<Reservation> getReservationById(Long id) {
        return reservationRepository.findById(id);
    }

    public Reservation addReservation(Reservation reservation) {
        return reservationRepository.save(reservation);
    }

    public void deleteReservation(long id) {
        Optional<Reservation> reservationOpt = reservationRepository.findById(id);
        if (reservationOpt.isPresent()) {
            Reservation reservation = reservationOpt.get();
            HotelRoom room = reservation.getRoom();
            if (room != null) {
                room.setAvailable(true);
                hotelRoomRepository.save(room);
            }
            reservationRepository.deleteById(id);
        } else {
            throw new RuntimeException("Reservation not found");
        }
    }

    public Reservation updateReservation(Reservation reservation) {
        return reservationRepository.save(reservation);
    }

    public List<Reservation> getReservationsByCustomerName(String name) {
        return reservationRepository.findByCustomerNameIgnoreCase(name);
    }

    public List<Reservation> getReservationsByRoomId(Long roomId) {
        return reservationRepository.findByRoomId(roomId);
    }

    public List<Reservation> getReservationsByDateRange(LocalDate start, LocalDate end) {
        return reservationRepository.findByCheckInDateBetween(start, end);
    }

    public boolean hasDateConflict(Long roomId, LocalDate checkIn, LocalDate checkOut) {
        List<Reservation> reservations = reservationRepository.findByRoomId(roomId);
        return reservations.stream().anyMatch(res ->
                !(res.getCheckOutDate().isBefore(checkIn) || res.getCheckInDate().isAfter(checkOut))
        );
    }
}
