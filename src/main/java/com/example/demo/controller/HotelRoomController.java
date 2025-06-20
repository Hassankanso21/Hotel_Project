package com.example.demo.controller;

import com.example.demo.dto.ReservationDTO;
import com.example.demo.entity.HotelRoom;
import com.example.demo.entity.Reservation;
import com.example.demo.repository.HotelRoomRepository;
import com.example.demo.repository.ReservationRepository;
import com.example.demo.service.HotelRoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class HotelRoomController {

    @Autowired
    private HotelRoomRepository hotelRoomRepository;

    @Autowired
    private HotelRoomService hotelRoomService;

    @Autowired
    private ReservationRepository reservationRepository;

    // ✅ Get all rooms
    @GetMapping("/rooms")
    public List<HotelRoom> getAllRooms() {
        return hotelRoomRepository.findAll();
    }

    // ✅ Search rooms
    @GetMapping("/rooms/search")
    public List<HotelRoom> searchRooms(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Integer minPrice,
            @RequestParam(required = false) Integer maxPrice,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut
    ) {
        return hotelRoomService.searchAvailableRooms(category, minPrice, maxPrice, checkIn, checkOut);
    }

    // ✅ Get room by room number
    @GetMapping("/rooms/number/{roomNumber}")
    public ResponseEntity<HotelRoom> getRoomByRoomNumber(@PathVariable String roomNumber) {
        return hotelRoomRepository.findByRoomNumber(roomNumber)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ✅ Get room by ID
    @GetMapping("/rooms/{id}")
    public ResponseEntity<HotelRoom> getRoomById(@PathVariable Long id) {
        return hotelRoomRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ✅ Create new room
    @PostMapping("/rooms")
    public ResponseEntity<HotelRoom> createRoom(@RequestBody HotelRoom room) {
        HotelRoom saved = hotelRoomRepository.save(room);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // ✅ Update room
    @PutMapping("/rooms/{id}")
    public ResponseEntity<?> updateRoom(@PathVariable Long id, @RequestBody HotelRoom roomDetails) {
        Optional<HotelRoom> opt = hotelRoomRepository.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Room not found.");
        }

        HotelRoom room = opt.get();
        room.setRoomNumber(roomDetails.getRoomNumber());
        room.setCategory(roomDetails.getCategory());
        room.setAvailable(roomDetails.isAvailable());
        room.setPricePerNight(roomDetails.getPricePerNight()); // ✅ Fix: update price too

        HotelRoom updated = hotelRoomRepository.save(room);
        return ResponseEntity.ok(updated);
    }

    // ✅ Delete room
    @DeleteMapping("/rooms/{id}")
    public ResponseEntity<?> deleteRoom(@PathVariable Long id) {
        if (!hotelRoomRepository.existsById(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Room not found.");
        }

        hotelRoomRepository.deleteById(id);
        return ResponseEntity.ok("Room deleted.");
    }

    // ✅ Get only available rooms
    @GetMapping("/rooms/available")
    public List<HotelRoom> getAvailableRooms() {
        return hotelRoomRepository.findByAvailableTrue();
    }

    // ✅ Get rooms by category
    @GetMapping("/rooms/category/{category}")
    public List<HotelRoom> getRoomsByCategory(@PathVariable String category) {
        return hotelRoomRepository.findByCategoryIgnoreCase(category);
    }

    // ✅ Get reservations by customer name
    @GetMapping("/reservations/customer/{name}")
    public List<ReservationDTO> getReservationsByCustomer(@PathVariable String name) {
        return reservationRepository.findByCustomerNameIgnoreCase(name)
                .stream()
                .map(ReservationDTO::new)
                .collect(Collectors.toList());
    }

    // ✅ Get reservations by room ID
    @GetMapping("/reservations/room/{roomId}")
    public List<ReservationDTO> getReservationsByRoomId(@PathVariable Long roomId) {
        return reservationRepository.findByRoomId(roomId)
                .stream()
                .map(ReservationDTO::new)
                .collect(Collectors.toList());
    }

    // ✅ Get reservations in a date range
    @GetMapping("/reservations/date-range")
    public List<ReservationDTO> getReservationsByDateRange(
            @RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return reservationRepository.findByCheckInDateBetween(start, end)
                .stream()
                .map(ReservationDTO::new)
                .collect(Collectors.toList());
    }

    // Total number of rooms
    @GetMapping("/rooms/count")
    public Long getTotalRooms() {
        return hotelRoomRepository.count();
    }

    // Number of available rooms
    @GetMapping("/rooms/available/count")
    public Long getAvailableRoomsCount() {
        return hotelRoomRepository.countByAvailableTrue();
    }

}
