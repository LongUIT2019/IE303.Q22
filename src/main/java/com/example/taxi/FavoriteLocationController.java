package com.example.taxi;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/favorites")
public class FavoriteLocationController {

    @Autowired
    private FirestoreUserRepository userRepository;

    @GetMapping
    public ResponseEntity<?> getFavorites(Authentication authentication) {
        if (authentication == null) return ResponseEntity.status(401).body("Unauthorized");
        String username = authentication.getName();
        AppUser user = userRepository.findByUsername(username).orElse(null);
        if (user == null) return ResponseEntity.status(404).body("User not found");
        return ResponseEntity.ok(user.getFavorites());
    }

    @PostMapping
    public ResponseEntity<?> addFavorite(Authentication authentication, @RequestBody FavoriteLocation favorite) {
        if (authentication == null) return ResponseEntity.status(401).body("Unauthorized");
        String username = authentication.getName();
        AppUser user = userRepository.findByUsername(username).orElse(null);
        if (user == null) return ResponseEntity.status(404).body("User not found");

        if (favorite.getId() == null || favorite.getId().isEmpty()) {
            favorite.setId(UUID.randomUUID().toString());
        }

        user.getFavorites().add(favorite);
        userRepository.save(user);
        return ResponseEntity.ok(favorite);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteFavorite(Authentication authentication, @PathVariable("id") String id) {
        if (authentication == null) return ResponseEntity.status(401).body("Unauthorized");
        String username = authentication.getName();
        AppUser user = userRepository.findByUsername(username).orElse(null);
        if (user == null) return ResponseEntity.status(404).body("User not found");

        user.getFavorites().removeIf(f -> f.getId().equals(id));
        userRepository.save(user);
        return ResponseEntity.ok("Deleted");
    }
}
