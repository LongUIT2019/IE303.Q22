package com.example.taxi;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class PricingService {

    @Autowired
    private DemandPredictionService demandPredictionService;

    // Tuple trả kết quả gồm Pricing và ML Details
    public static class PricingResult {
        public String zone;
        public double distanceKm;
        public double estimatedPriceVnd;
        public double mlMultiplier;
    }

    /**
     * Logic Tính Giá và Điều Hướng Cốt Lõi:
     * 1. Nhận toạ độ Khách hàng
     * 2. Phân loại Zone địa lý
     * 3. Gọi Machine Learning để tính Multiplier (Surge)
     * 4. Tính Total Price
     */
    public PricingResult calculateSurgePrice(double pickupLat, double pickupLng, double dropLat, double dropLng,
            boolean isRain, boolean isEvent) {
        // 1. Phân loại Khung vực (Demo Geofencing cho khu vực TP.HCM - 6 Vùng)
        String zone;
        if (pickupLat >= 10.80) {
            zone = (pickupLng < 106.66) ? "Zone_NW" : "Zone_NE"; // Phía Sân bay / Gò Vấp & Thủ Đức
        } else if (pickupLat >= 10.75) {
            zone = (pickupLng < 106.66) ? "Zone_CW" : "Zone_CE"; // Trung tâm Tây / Trung tâm Đông (Q1, Q3)
        } else {
            zone = (pickupLng < 106.66) ? "Zone_SW" : "Zone_SE"; // Ngoại ô Nam (Bình Chánh / Q7)
        }

        // 2. Machine Learning Pipeline (Nhận số lượng cuốc xe dự báo)
        boolean finalRain = isRain || demandPredictionService.isSimulatedRain();
        boolean finalEvent = isEvent || demandPredictionService.isSimulatedEvent();
        Map<String, Integer> aiDemands = demandPredictionService.predictCurrentDemand(finalRain, finalEvent);
        int demandForZone = aiDemands.getOrDefault(zone, 15);

        // ML Multiplier: Base Demand là 15 (Normal), nhân với trọng số của từng zone để chuẩn hóa base demand.
        // Nếu Demand > baseDemandForZone = Nhu cầu cao -> Gắn giá Surge (Tối đa x3)
        double zoneWeight = 1.0;
        if (zone.equals("Zone_NW")) zoneWeight = 1.5;
        else if (zone.equals("Zone_NE")) zoneWeight = 1.8;
        else if (zone.equals("Zone_CW")) zoneWeight = 2.5;
        else if (zone.equals("Zone_CE")) zoneWeight = 3.5;
        else if (zone.equals("Zone_SW")) zoneWeight = 0.8;
        else if (zone.equals("Zone_SE")) zoneWeight = 1.0;

        double baseDemandForZone = 15.0 * zoneWeight;
        double rawMultiplier = (double) demandForZone / baseDemandForZone;
        double surgeMultiplier = Math.max(1.0, Math.min(rawMultiplier, 3.0)); // Giới hạn Multiplier từ 1x đến 3x

        // 3. Tính khoảng cách đường chim bay (Haversine Formula) bằng KM
        double distance = calculateHaversineDistance(pickupLat, pickupLng, dropLat, dropLng);
        if (distance < 1.0)
            distance = 1.0; // Tối thiểu 1km

        // 4. Tính Tiền
        double baseFarePerKm = 15000.0; // 15,000 VND / km
        double finalPrice = distance * baseFarePerKm * surgeMultiplier;

        // Trả kết quả
        PricingResult result = new PricingResult();
        result.zone = zone;
        result.distanceKm = distance;
        result.estimatedPriceVnd = finalPrice;
        result.mlMultiplier = surgeMultiplier;
        return result;
    }

    // Haversine Algorithm để đo khoảng cách GPS
    private double calculateHaversineDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Earth radius in km
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                        * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}
