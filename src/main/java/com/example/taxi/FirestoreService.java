package com.example.taxi;

import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Service duy nhất để thao tác Ride và Driver trên Firestore.
 * Không còn in-memory fallback — mọi thứ đều lưu thật trên Firestore.
 */
@Service
public class FirestoreService {

    @Autowired
    private Firestore firestore;

    // ==================== RIDE ====================

    /** Lưu hoặc cập nhật Ride. Nếu id null thì tự sinh UUID mới. */
    public Ride saveRide(Ride ride) throws Exception {
        if (ride.getId() == null || ride.getId().isEmpty()) {
            ride.setId(UUID.randomUUID().toString());
        }
        firestore.collection("rides").document(ride.getId()).set(ride).get();
        return ride;
    }

    /** Lấy Ride theo ID */
    public Ride getRide(String id) throws Exception {
        var doc = firestore.collection("rides").document(id).get().get();
        if (!doc.exists()) return null;
        return doc.toObject(Ride.class);
    }

    /** Lấy tất cả rides */
    public List<Ride> getAllRides() throws Exception {
        List<Ride> rides = new ArrayList<>();
        for (QueryDocumentSnapshot doc : firestore.collection("rides").get().get().getDocuments()) {
            Ride r = doc.toObject(Ride.class);
            if (r != null) rides.add(r);
        }
        return rides;
    }

    /** Lấy rides theo tên khách hàng */
    public List<Ride> findRidesByCustomerName(String customerName) throws Exception {
        List<Ride> rides = new ArrayList<>();
        for (QueryDocumentSnapshot doc : firestore.collection("rides")
                .whereEqualTo("customerName", customerName)
                .get().get().getDocuments()) {
            Ride r = doc.toObject(Ride.class);
            if (r != null) rides.add(r);
        }
        return rides;
    }

    /** Lấy rides theo driverId */
    public List<Ride> findRidesByDriverId(String driverId) throws Exception {
        List<Ride> rides = new ArrayList<>();
        for (QueryDocumentSnapshot doc : firestore.collection("rides")
                .whereEqualTo("driverId", driverId)
                .get().get().getDocuments()) {
            Ride r = doc.toObject(Ride.class);
            if (r != null) rides.add(r);
        }
        return rides;
    }

    /** Cập nhật status của một ride */
    public Ride updateRideStatus(String rideId, String status) throws Exception {
        Ride ride = getRide(rideId);
        if (ride == null) return null;
        ride.setStatus(status);
        firestore.collection("rides").document(rideId).set(ride).get();
        return ride;
    }

    // ==================== DRIVER ====================

    /** Lưu hoặc cập nhật Driver */
    public void saveDriver(Driver driver) throws Exception {
        firestore.collection("drivers").document(driver.getId()).set(driver).get();
    }

    /** Lấy tất cả driver đang online */
    public List<Driver> getOnlineDrivers() throws Exception {
        List<Driver> drivers = new ArrayList<>();
        for (QueryDocumentSnapshot doc : firestore.collection("drivers")
                .whereEqualTo("online", true)
                .get().get().getDocuments()) {
            Driver d = doc.toObject(Driver.class);
            if (d != null) drivers.add(d);
        }
        return drivers;
    }
}
