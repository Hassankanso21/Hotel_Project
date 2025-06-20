package com.example.demo.repository;

import com.example.demo.entity.HotelRoom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface HotelRoomRepository extends JpaRepository<HotelRoom, Long> {

    List<HotelRoom> findByAvailableTrue();

    List<HotelRoom> findByCategoryIgnoreCase(String category);

    Optional<HotelRoom> findByRoomNumber(String roomNumber); // ✅ Added for getRoomByNumber()

    long countByAvailableTrue();

}
