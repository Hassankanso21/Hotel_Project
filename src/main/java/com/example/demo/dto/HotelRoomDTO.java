package com.example.demo.dto;

import com.example.demo.entity.HotelRoom;

public class HotelRoomDTO {
    private Long id;
    private String roomNumber;
    private String category;
    private boolean available;
    private double pricePerNight;

    public HotelRoomDTO() {}

    public HotelRoomDTO(HotelRoom room) {
        this.id = room.getId();
        this.roomNumber = room.getRoomNumber();
        this.category = room.getCategory();
        this.available = room.isAvailable();
        this.pricePerNight = room.getPricePerNight();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public double getPricePerNight() {
        return pricePerNight;
    }

    public void setPricePerNight(double pricePerNight) {
        this.pricePerNight = pricePerNight;
    }

    @Override
    public String toString() {
        return "HotelRoomDTO{" +
                "id=" + id +
                ", roomNumber='" + roomNumber + '\'' +
                ", category='" + category + '\'' +
                ", available=" + available +
                ", pricePerNight=" + pricePerNight +
                '}';
    }
}
