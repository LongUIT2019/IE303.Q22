package com.example.taxi;

public class Ride {
    private String id;
    private String customerName;
    private String status; // REQUESTED, ACCEPTED, ARRIVED, STARTED, COMPLETED
    private String driverId;

    // Hệ thống toạ độ bản đồ (Leaflet/Map Integration)
    private double pickupLat;
    private double pickupLng;
    private double dropoffLat;
    private double dropoffLng;

    // Hệ thống AI Pricing (Surge Pricing)
    private String zone;
    private double estimatedPrice;
    private double mlMultiplier;

    public Ride() {
        // Required empty constructor for Firestore
    }

    public Ride(String customerName) {
        this.customerName = customerName;
        this.status = "REQUESTED";
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDriverId() {
        return driverId;
    }

    public void setDriverId(String driverId) {
        this.driverId = driverId;
    }

    public double getPickupLat() {
        return pickupLat;
    }

    public void setPickupLat(double pickupLat) {
        this.pickupLat = pickupLat;
    }

    public double getPickupLng() {
        return pickupLng;
    }

    public void setPickupLng(double pickupLng) {
        this.pickupLng = pickupLng;
    }

    public double getDropoffLat() {
        return dropoffLat;
    }

    public void setDropoffLat(double dropoffLat) {
        this.dropoffLat = dropoffLat;
    }

    public double getDropoffLng() {
        return dropoffLng;
    }

    public void setDropoffLng(double dropoffLng) {
        this.dropoffLng = dropoffLng;
    }

    public String getZone() {
        return zone;
    }

    public void setZone(String zone) {
        this.zone = zone;
    }

    public double getEstimatedPrice() {
        return estimatedPrice;
    }

    public void setEstimatedPrice(double estimatedPrice) {
        this.estimatedPrice = estimatedPrice;
    }

    public double getMlMultiplier() {
        return mlMultiplier;
    }

    public void setMlMultiplier(double mlMultiplier) {
        this.mlMultiplier = mlMultiplier;
    }
}
