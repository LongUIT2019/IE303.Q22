package com.example.taxi;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/rides")
public class RideController {

    @Autowired
    private FirestoreService firestoreService;

    @Autowired
    private PricingService pricingService;

    @PostMapping("/request")
    public ResponseEntity<?> requestRide(
            @RequestParam("customerName") String customerName,
            @RequestParam("pickupLat") double pickupLat,
            @RequestParam("pickupLng") double pickupLng,
            @RequestParam("dropoffLat") double dropoffLat,
            @RequestParam("dropoffLng") double dropoffLng,
            @RequestParam(value = "isRain", defaultValue = "false") boolean isRain,
            @RequestParam(value = "isEvent", defaultValue = "false") boolean isEvent) {

        try {
            PricingService.PricingResult pResult = pricingService.calculateSurgePrice(pickupLat, pickupLng, dropoffLat,
                    dropoffLng, isRain, isEvent);

            Ride ride = new Ride(customerName);
            ride.setStatus("REQUESTED");
            ride.setPickupLat(pickupLat);
            ride.setPickupLng(pickupLng);
            ride.setDropoffLat(dropoffLat);
            ride.setDropoffLng(dropoffLng);
            ride.setZone(pResult.zone);
            ride.setEstimatedPrice(Math.round(pResult.estimatedPriceVnd));
            ride.setMlMultiplier(pResult.mlMultiplier);

            Ride saved = firestoreService.saveRide(ride);
            return ResponseEntity.ok(saved);

        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", "Lỗi Server/Config: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PostMapping("/{rideId}/accept")
    public ResponseEntity<?> acceptRide(@PathVariable("rideId") String rideId,
            @RequestParam("driverId") String driverId) {
        try {
            Ride ride = firestoreService.getRide(rideId);
            if (ride != null && ride.getStatus().equals("REQUESTED")) {
                ride.setDriverId(driverId);
                ride.setStatus("ACCEPTED");
                firestoreService.saveRide(ride);
                return ResponseEntity.ok(ride);
            } else {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Chuyến đi không tồn tại hoặc đã bị nhận!");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", "Lỗi Firestore: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PostMapping("/{rideId}/status")
    public ResponseEntity<?> updateRideStatus(@PathVariable("rideId") String rideId,
            @RequestParam("status") String status) {
        try {
            Ride ride = firestoreService.updateRideStatus(rideId, status);
            if (ride != null) {
                return ResponseEntity.ok(ride);
            } else {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Chuyến đi không tồn tại!");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", "Lỗi Firestore: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PostMapping("/{rideId}/start")
    public ResponseEntity<?> startRide(@PathVariable("rideId") String rideId) {
        try {
            Ride ride = firestoreService.getRide(rideId);
            if (ride != null && ride.getStatus().equals("ACCEPTED")) {
                ride.setStatus("STARTED");
                firestoreService.saveRide(ride);
                return ResponseEntity.ok(ride);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Không thể bắt đầu chuyến đi này!");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi: " + e.getMessage());
        }
    }

    @PostMapping("/{rideId}/complete")
    public ResponseEntity<?> completeRide(@PathVariable("rideId") String rideId) {
        try {
            Ride ride = firestoreService.getRide(rideId);
            if (ride != null && ride.getStatus().equals("STARTED")) {
                ride.setStatus("COMPLETED");
                firestoreService.saveRide(ride);
                return ResponseEntity.ok(ride);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Không thể kết thúc chuyến đi này!");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi: " + e.getMessage());
        }
    }

    @PostMapping("/{rideId}/cancel")
    public ResponseEntity<?> cancelRide(@PathVariable("rideId") String rideId) {
        try {
            Ride ride = firestoreService.getRide(rideId);
            if (ride != null) {
                ride.setStatus("CANCELLED");
                firestoreService.saveRide(ride);
                return ResponseEntity.ok(ride);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Chuyến đi không tồn tại!");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllRides() {
        try {
            List<Ride> rides = firestoreService.getAllRides();
            return ResponseEntity.ok(rides);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi lấy dữ liệu: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getRide(@PathVariable("id") String id) {
        try {
            Ride ride = firestoreService.getRide(id);
            if (ride == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Không tìm thấy chuyến đi!");
            }
            return ResponseEntity.ok(ride);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi lấy dữ liệu: " + e.getMessage());
        }
    }
}
