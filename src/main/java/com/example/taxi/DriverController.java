package com.example.taxi;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/drivers")
public class DriverController {

    @Autowired
    private FirestoreService firestoreService;

    // Đánh dấu tài xế bắt đầu online
    @PostMapping("/{id}/online")
    public Driver setOnline(@PathVariable("id") String id, @RequestParam("name") String name) throws Exception {
        Driver driver = new Driver(id, name, true);
        firestoreService.saveDriver(driver);
        return driver;
    }

    // Đánh dấu tài xế offline
    @PostMapping("/{id}/offline")
    public Driver setOffline(@PathVariable("id") String id, @RequestParam("name") String name) throws Exception {
        Driver driver = new Driver(id, name, false);
        firestoreService.saveDriver(driver);
        return driver;
    }
}
