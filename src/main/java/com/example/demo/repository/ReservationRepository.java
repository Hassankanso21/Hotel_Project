package com.example.demo.repository;

import com.example.demo.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    @Query("SELECT r FROM Reservation r JOIN FETCH r.room")
    List<Reservation> findAllWithRoom();

    List<Reservation> findByCustomerNameIgnoreCase(String name);

    List<Reservation> findByRoomId(Long roomId);

    List<Reservation> findByCheckInDateBetween(LocalDate start, LocalDate end);

    long countByCheckOutDateAfterOrCheckOutDateEquals(LocalDate after, LocalDate sameDay);

}
