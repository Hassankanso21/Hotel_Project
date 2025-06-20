package com.example.demo.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "hotel_room")
public class HotelRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String roomNumber;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false)
    private boolean available;

    @Column(nullable = false)
    private double pricePerNight;

    public HotelRoom() {}

    public HotelRoom(String roomNumber, String category, boolean available, double pricePerNight) {
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
        return "HotelRoom{" +
                "id=" + id +
                ", roomNumber='" + roomNumber + '\'' +
                ", category='" + category + '\'' +
                ", available=" + available +
                ", pricePerNight=" + pricePerNight +
                '}';
    }
}
