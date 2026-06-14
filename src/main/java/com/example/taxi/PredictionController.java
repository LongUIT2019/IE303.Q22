package com.example.taxi;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

import java.util.Map;

/**
 * REST API Exposing Machine Learning Predictions.
 */
@RestController
@RequestMapping("/api/ml")
public class PredictionController {

    @Autowired
    private DemandPredictionService predictionService;

    @GetMapping("/predict/current")
    public Map<String, Integer> predictCurrent(
            @RequestParam(value = "isRain", defaultValue = "false") boolean isRain,
            @RequestParam(value = "isEvent", defaultValue = "false") boolean isEvent,
            Authentication authentication) {

        boolean finalRain = isRain;
        boolean finalEvent = isEvent;

        if (authentication != null) {
            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            if (isAdmin) {
                System.out.println("[AUDIT LOG - ADMIN ACTION] Tên tài khoản '" + authentication.getName()
                        + "' đã thay đổi biến môi trường: Mô phỏng Mưa=" + isRain + ", Sự kiện=" + isEvent);
                predictionService.setSimulatedRain(isRain);
                predictionService.setSimulatedEvent(isEvent);
            } else {
                finalRain = predictionService.isSimulatedRain();
                finalEvent = predictionService.isSimulatedEvent();
            }
        } else {
            finalRain = predictionService.isSimulatedRain();
            finalEvent = predictionService.isSimulatedEvent();
        }

        return predictionService.predictCurrentDemand(finalRain, finalEvent);
    }
}
