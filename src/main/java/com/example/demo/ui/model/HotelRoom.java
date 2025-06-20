package com.example.demo.ui.model;

public class HotelRoom {
    private Long id;
    private String roomNumber;
    private String category;
    private boolean available;
    private double pricePerNight;

    public HotelRoom() {
    }

    public HotelRoom(Long id, String roomNumber, String category, boolean available, double pricePerNight) {
        this.id = id;
        this.roomNumber = roomNumber;
        this.category = category;
        this.available = available;
        this.pricePerNight = pricePerNight;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }

    public double getPricePerNight() { return pricePerNight; }
    public void setPricePerNight(double pricePerNight) { this.pricePerNight = pricePerNight; }

    @Override
    public String toString() {
        return roomNumber + " (" + category + ")";
    }
}
