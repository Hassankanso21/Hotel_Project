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
import java.util.stream.Collectors;

@Service
public class HotelRoomService {

    @Autowired
    private HotelRoomRepository hotelRoomRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    public List<HotelRoom> getAllRooms() {
        return hotelRoomRepository.findAll();
    }

    public Optional<HotelRoom> getRoomById(Long id) {
        return hotelRoomRepository.findById(id);
    }

    public Optional<HotelRoom> getRoomByNumber(String roomNumber) {
        return hotelRoomRepository.findByRoomNumber(roomNumber);
    }

    public HotelRoom addRoom(HotelRoom room) {
        return hotelRoomRepository.save(room);
    }

    public void deleteRoom(Long id) {
        hotelRoomRepository.deleteById(id);
    }

    public HotelRoom updateRoom(HotelRoom room) {
        return hotelRoomRepository.save(room);
    }

    public List<HotelRoom> searchAvailableRooms(String category, Integer minPrice, Integer maxPrice,
                                                LocalDate checkIn, LocalDate checkOut) {
        List<HotelRoom> all = hotelRoomRepository.findAll();

        return all.stream()
                .filter(room -> category == null || room.getCategory().equalsIgnoreCase(category))
                .filter(room -> minPrice == null || room.getPricePerNight() >= minPrice)
                .filter(room -> maxPrice == null || room.getPricePerNight() <= maxPrice)
                .filter(HotelRoom::isAvailable)
                .filter(room -> {
                    List<Reservation> reservations = reservationRepository.findByRoomId(room.getId());
                    return reservations.stream().noneMatch(res ->
                            !(res.getCheckOutDate().isBefore(checkIn) || res.getCheckInDate().isAfter(checkOut))
                    );
                })
                .collect(Collectors.toList());
    }

    public List<HotelRoom> getAvailableRooms() {
        return hotelRoomRepository.findByAvailableTrue();
    }

    public List<HotelRoom> getRoomsByCategory(String category) {
        return hotelRoomRepository.findByCategoryIgnoreCase(category);
    }
}
