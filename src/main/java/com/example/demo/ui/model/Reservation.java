package com.example.demo.ui.model;

import java.time.LocalDate;

public class Reservation {
    private Long id;
    private String customerName;

    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private boolean paymentStatus;
    private Long roomId;

    public Reservation() {}

    public Reservation(Long id, String customerName, HotelRoom room, LocalDate checkInDate, LocalDate checkOutDate, boolean paymentStatus) {
        this.id = id;
        this.customerName = customerName;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.paymentStatus = paymentStatus;
    }

 // Add this field

    public Long getRoomId() {
        return roomId;
    }
    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }


    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }



    public LocalDate getCheckInDate() { return checkInDate; }
    public void setCheckInDate(LocalDate checkInDate) { this.checkInDate = checkInDate; }

    public LocalDate getCheckOutDate() { return checkOutDate; }
    public void setCheckOutDate(LocalDate checkOutDate) { this.checkOutDate = checkOutDate; }

    public boolean isPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(boolean paymentStatus) { this.paymentStatus = paymentStatus; }


}
