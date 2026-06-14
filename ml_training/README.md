# 🧠 Machine Learning Training Pipeline

Thư mục này chứa toàn bộ quy trình Offline Learning để huấn luyện mô hình Trí Tuệ Nhân Tạo (Predictive Demand Model) nhằm phục vụ việc tính toán Tỉ lệ Phụ thu (Surge Pricing) cho ứng dụng Taxi Dispatch.

## Kiến trúc Native Inference Interface

Thông thường, một Model ML sẽ được deploy thành 1 Microservice Python riêng biệt (dùng FastAPI/Flask) gọi qua API. Nhưng cách đó gây hao tốn hệ thống và tăng độ trễ (Network Latency) do Spring Boot phải liên tục Wait.

Với phương án triển khai của ứng dụng này:
1. Python thực hiện rèn luyện **Poisson Regression** trên thư viện `scikit-learn`.
2. Bóc tách và xuất các Thông số (Coefficients / Intercept) lưu dưới dạng file `model_weights.json`.
3. Java Spring Boot (`DemandPredictionService.java`) lúc khởi động sẽ tự động đọc ngược lại file JSON trên để cấu trúc thành một **phương trình đại số tĩnh**, giúp phục vụ lượng lớn Data realtime mà không có độ trễ. 

## Cấu Trúc Tập Lệnh

- `generate_and_train.py`: Chịu trách nhiệm tạo giả lập 100,000 dữ liệu mẫu (mock dataset) đúc kết từ bối cảnh Giờ Cao Điểm, Trời Mưa, Sự Kiện. Sau đó chạy Fit cho mô hình Scikit-Learn và tự động sinh file JSON trọng số.
- `taxi_demand_dataset.csv`: (Generated) Tập dữ liệu đào tạo.
- `model_weights.json`: (Generated) File thông số tích hợp cầu nối của luồng Model và Core Backend Java.

## Cấu Trúc Dữ Liệu CSV (Dataset Features)

Bộ dữ liệu `taxi_demand_dataset.csv` mang đặc trưng của **Dữ liệu Đếm theo Chuỗi Thời Gian (Time-series Count Data)** với 6 cột chính:
1. `timestamp`: Mốc thời gian theo từng giờ (VD: `2023-01-01 00:00:00`).
2. `hour`: Cột bóc tách giờ (0-23) để máy học dễ dàng tìm ra chu kỳ luân hồi trong ngày (Pattern recognition).
3. `is_rush_hour`: Feature phân dải. Đánh số `1` nếu vào khung giờ đi làm (7h-9h) hoặc tan tầm (17h-19h).
4. `is_rain`: Feature thời tiết. Đánh số `1` nếu trời mưa (Set ngẫu nhiên xác suất 15%).
5. `is_event`: Feature ngoại cảnh. Đánh số `1` nếu có sự kiện/kẹt xe lớn (Set ngẫu nhiên xác suất 5%).
6. `total_demand` *(Target Label)*: Tổng lượng cuốc xe thực tế được ghi nhận trong giờ đó. Thuật toán Poisson sẽ nhìn vào 5 cột đầu tiên để học và tìm ra quy luật suy ra con số Nhu Cầu ở cột này.

## Hướng dẫn Run

Nếu muốn AI học lại từ đầu và cập nhật bộ thông số cho Backend Spring Boot, hãy làm các bước sau:

```bash
cd ml_training

# 1. Khởi tạo và thiết lập Virtual Env (Chỉ cần làm lần đầu)
python3 -m venv venv
.\venv\Scripts\activate
pip install pandas scikit-learn numpy

# 2. Chạy Python AI Script
python3 generate_and_train.py
```
Sau bước này, hãy khởi động lại Spring Boot để hệ thống nạp (load) thông số Machine Learning mới nhất.
