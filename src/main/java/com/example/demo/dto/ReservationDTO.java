package com.example.demo.dto;

import com.example.demo.entity.Reservation;
import java.time.LocalDate;

public class ReservationDTO {
    private Long id;
    private String customerName;
    private Long roomId;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private boolean paymentStatus;

    public ReservationDTO() {}

    public ReservationDTO(Reservation res) {
        this.id = res.getId();
        this.customerName = res.getCustomerName();
        this.roomId = (res.getRoom() != null) ? res.getRoom().getId() : null;
        this.checkInDate = res.getCheckInDate();
        this.checkOutDate = res.getCheckOutDate();
        this.paymentStatus = res.isPaymentStatus();

        if (this.roomId == null) {
            System.err.println("⚠️ Warning: Reservation with ID " + id + " has no Room assigned.");
        }
    }


    public ReservationDTO(Long id, String customerName, Long roomId, LocalDate checkInDate, LocalDate checkOutDate, boolean paymentStatus) {
        this.id = id;
        this.customerName = customerName;
        this.roomId = roomId;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.paymentStatus = paymentStatus;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public Long getRoomId() { return roomId; }
    public void setRoomId(Long roomId) { this.roomId = roomId; }

    public LocalDate getCheckInDate() { return checkInDate; }
    public void setCheckInDate(LocalDate checkInDate) { this.checkInDate = checkInDate; }

    public LocalDate getCheckOutDate() { return checkOutDate; }
    public void setCheckOutDate(LocalDate checkOutDate) { this.checkOutDate = checkOutDate; }

    public boolean isPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(boolean paymentStatus) { this.paymentStatus = paymentStatus; }

    @Override
    public String toString() {
        return "ReservationDTO{" +
                "id=" + id +
                ", customerName='" + customerName + '\'' +
                ", roomId=" + roomId +
                ", checkInDate=" + checkInDate +
                ", checkOutDate=" + checkOutDate +
                ", paymentStatus=" + paymentStatus +
                '}';
    }
}
