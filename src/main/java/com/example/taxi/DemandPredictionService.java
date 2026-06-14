package com.example.taxi;

import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.File;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

/**
 * =========================================================================================
 * KIẾN TRÚC NHÚNG MÔ HÌNH MÁY HỌC DẠNG NATIVE (NATIVE INFERENCE ARCHITECTURE)
 * =========================================================================================
 * Thay vì tốn tài nguyên chạy một máy chủ Python riêng biệt để host mô hình (bị
 * giới hạn
 * tốc độ phản hồi qua HTTP Data Transfer), class này chuyển thể trực tiếp thuật
 * toán học được từ
 * Machine Learning (Poisson Regression) thành phương trình toán học thuần tuý
 * (Algebraic Equation).
 * 
 * LƯU Ý VẤN ĐÁP:
 * - Thuật toán Linear Regression là dạng CỘNG tuyến tính (Y = b0 + b1*X1 +
 * b2*X2...). Dễ bị ra kết quả nhu cầu âm.
 * - Thuật toán Poisson Regression chuyên xử lý dữ liệu COUNT DATA (Dữ liệu đếm
 * lượng xe) sử dụng Log-Link Function.
 * Do đó, các tác động đặc trưng (Features như Mưa, Sự kiện) sẽ nằm ở kết cấu
 * phép NHÂN (Multiplicative).
 * - Code Service này chính là sự chuyển hoá chính xác từ Log-Link Func: Y =
 * e^(w0 + w1*X1) = Base * Multiplier.
 */
@Service
public class DemandPredictionService {

    // Các thông số mặc định (Sẽ bị ghi đè nếu file JSON tồn tại)
    private double baseDemandIntercept = 15.0;
    private double rushHourMultiplier = 2.5;
    private double rainMultiplier = 1.3;
    private double eventMultiplier = 3.0;

    // Trạng thái giả lập thời tiết và sự kiện toàn hệ thống
    private boolean simulatedRain = false;
    private boolean simulatedEvent = false;

    public boolean isSimulatedRain() {
        return simulatedRain;
    }

    public void setSimulatedRain(boolean simulatedRain) {
        this.simulatedRain = simulatedRain;
    }

    public boolean isSimulatedEvent() {
        return simulatedEvent;
    }

    public void setSimulatedEvent(boolean simulatedEvent) {
        this.simulatedEvent = simulatedEvent;
    }

    public DemandPredictionService() {
        loadModelWeights();
    }

    private void loadModelWeights() {
        try {
            File jsonFile = new File("ml_training/model_weights.json");
            if (jsonFile.exists()) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(jsonFile);
                if (root.has("baseIntercept"))
                    baseDemandIntercept = root.get("baseIntercept").asDouble();
                if (root.has("rushHourMultiplier"))
                    rushHourMultiplier = root.get("rushHourMultiplier").asDouble();
                if (root.has("rainMultiplier"))
                    rainMultiplier = root.get("rainMultiplier").asDouble();
                if (root.has("eventMultiplier"))
                    eventMultiplier = root.get("eventMultiplier").asDouble();
                System.out.println("✅ Tải thành công tệp model_weights.json từ ML Python!");
            } else {
                System.out.println("⚠️ Không tìm thấy file model_weights.json, chạy với cấu hình mặc định.");
            }
        } catch (Exception e) {
            System.err.println("Lỗi đọc JSON ML: " + e.getMessage());
        }
    }

    public Map<String, Integer> predictCurrentDemand(boolean isRain, boolean isEvent) {
        int currentHour = LocalTime.now().getHour();
        return predictDemand(currentHour, isRain, isEvent);
    }

    /**
     * Thực thi tính toán Suy luận Model (Inference Process)
     */
    public Map<String, Integer> predictDemand(int hour, boolean isRain, boolean isEvent) {
        Map<String, Integer> predictions = new HashMap<>();

        // 1. TRỌNG SỐ THỜI GIAN (Feature: Time)
        // Nếu rơi vào giờ cao điểm Rush Hour (7h-9h hoặc 17h-19h), mô hình nhận diện
        // nhu cầu gọi tăng x lần.
        double timeWeight = ((hour >= 7 && hour <= 9) || (hour >= 17 && hour <= 19)) ? rushHourMultiplier : 1.0;

        // 2. TRỌNG SỐ MÔI TRƯỜNG & SỰ KIỆN (Feature: Weather & Event Context)
        // Chiết xuất (Extract) bộ Coefficients học từ Poisson Model
        double rainWeight = isRain ? rainMultiplier : 1.0;
        double eventWeight = isEvent ? eventMultiplier : 1.0;

        // 3. PHÉP TÍNH SUY LUẬN (INFERENCE EQUATION)
        // Áp dụng tính chất Multiplicative của Poisson để triệt tiêu số âm, giữ nguyên
        // sức mạnh phi tuyến.
        double calculatedBase = baseDemandIntercept * timeWeight * rainWeight * eventWeight;

        // 4. PHÂN BỔ ĐẦU RA THEO KHU VỰC (Zone Categorical Weights Output)
        // Làm tròn thành kiểu (int) bởi vì biến mục tiêu (target) là số cuốc xe luôn
        // luôn phải là số nguyên dương.
        predictions.put("Zone_NW", (int) Math.round(calculatedBase * 1.5)); // Khu Tây Bắc Sân Bay
        predictions.put("Zone_NE", (int) Math.round(calculatedBase * 1.8)); // Khu Đông Bắc Thủ Đức
        predictions.put("Zone_CW", (int) Math.round(calculatedBase * 2.5)); // Trung tâm Tây
        predictions.put("Zone_CE", (int) Math.round(calculatedBase * 3.5)); // Trung tâm Q1, Q3 (Nhu cầu cao nhất)
        predictions.put("Zone_SW", (int) Math.round(calculatedBase * 0.8)); // Cửa ngõ Ngoại ô Nam
        predictions.put("Zone_SE", (int) Math.round(calculatedBase * 1.0)); // Cửa ngõ Đông Nam

        return predictions;
    }
}
