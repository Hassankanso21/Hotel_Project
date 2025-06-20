package com.example.demo.controller;

import com.example.demo.dto.ReservationDTO;
import com.example.demo.entity.HotelRoom;
import com.example.demo.entity.Reservation;
import com.example.demo.repository.HotelRoomRepository;
import com.example.demo.repository.ReservationRepository;
import com.example.demo.service.ReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/reservations")
public class ReservationController {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private HotelRoomRepository hotelRoomRepository;

    @GetMapping
    public List<ReservationDTO> getAllReservations() {
        return reservationService.getAllReservations().stream()
                .filter(res -> res.getRoom() != null)
                .map(ReservationDTO::new)
                .toList();

    }



    @GetMapping("/{id}")
    public ResponseEntity<?> getReservationById(@PathVariable Long id) {
        Optional<Reservation> resOpt = reservationService.getReservationById(id);
        if (resOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Reservation not found.");
        }
        return ResponseEntity.ok(new ReservationDTO(resOpt.get()));
    }

    @PostMapping
    public ResponseEntity<?> addReservation(@RequestBody ReservationDTO dto) {
        if (dto.getRoomId() == null) {
            return ResponseEntity.badRequest().body("Room ID is required.");
        }

        Optional<HotelRoom> roomOpt = hotelRoomRepository.findById(dto.getRoomId());
        if (roomOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Invalid Room ID.");
        }

        boolean conflict = reservationService.hasDateConflict(
                dto.getRoomId(),
                dto.getCheckInDate(),
                dto.getCheckOutDate()
        );

        if (conflict) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Room is already booked for the selected dates.");
        }

        Reservation newRes = new Reservation();
        newRes.setCustomerName(dto.getCustomerName());
        newRes.setRoom(roomOpt.get());
        newRes.setCheckInDate(dto.getCheckInDate());
        newRes.setCheckOutDate(dto.getCheckOutDate());
        newRes.setPaymentStatus(dto.isPaymentStatus());

        Reservation saved = reservationService.addReservation(newRes);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ReservationDTO(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateReservation(@PathVariable Long id, @RequestBody ReservationDTO dto) {
        Optional<Reservation> resOpt = reservationService.getReservationById(id);
        if (resOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Reservation not found.");
        }

        Reservation resToUpdate = resOpt.get();
        resToUpdate.setCustomerName(dto.getCustomerName());
        resToUpdate.setCheckInDate(dto.getCheckInDate());
        resToUpdate.setCheckOutDate(dto.getCheckOutDate());
        resToUpdate.setPaymentStatus(dto.isPaymentStatus());

        if (dto.getRoomId() != null) {
            Optional<HotelRoom> roomOpt = hotelRoomRepository.findById(dto.getRoomId());
            if (roomOpt.isEmpty()) {
                return ResponseEntity.badRequest().body("Invalid Room ID.");
            }
            resToUpdate.setRoom(roomOpt.get());
        }

        Reservation updated = reservationService.updateReservation(resToUpdate);
        return ResponseEntity.ok(new ReservationDTO(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteReservation(@PathVariable long id) {
        Optional<Reservation> resOpt = reservationService.getReservationById(id);
        if (resOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Reservation not found.");
        }

        reservationService.deleteReservation(id);
        return ResponseEntity.ok("Reservation deleted.");
    }

    @Autowired
    private ReservationRepository reservationRepository;

    // Total number of reservations
    @GetMapping("/count")
    public Long getTotalReservations() {
        return reservationRepository.count();
    }

    // Number of active (future or ongoing) reservations
    @GetMapping("/active/count")
    public Long getActiveReservationsCount() {
        return reservationRepository.countByCheckOutDateAfterOrCheckOutDateEquals(LocalDate.now(), LocalDate.now());
    }

    @PutMapping("/{id}/pay")
    public ResponseEntity<?> markReservationAsPaid(@PathVariable Long id) {
        Optional<Reservation> resOpt = reservationService.getReservationById(id);
        if (resOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Reservation not found.");
        }

        Reservation reservation = resOpt.get();
        if (reservation.isPaymentStatus()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Reservation is already marked as paid.");
        }

        reservation.setPaymentStatus(true);
        reservationService.updateReservation(reservation);
        return ResponseEntity.noContent().build();
    }


}
