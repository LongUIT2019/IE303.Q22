import pandas as pd
import numpy as np
from sklearn.linear_model import PoissonRegressor
from sklearn.model_selection import train_test_split
from sklearn.metrics import mean_poisson_deviance
import os

# ==========================================
# PHẦN 1: TẠO DỮ LIỆU LỊCH SỬ GIẢ LẬP (MOCK DATASET)
# ==========================================
print("Đang khởi tạo dữ liệu lịch sử gọi xe...")
np.random.seed(42)

# Giả lập 100000 giờ thu thập dữ liệu (~11 năm)
num_samples = 100000

# Các cột (Features): Hour (0-23), Is_Rain (0/1), Is_Event (0/1)
# Kết thúc vào lúc hiện tại 23/04/2026
timestamps = pd.date_range(end="2026-04-23 21:00:00", periods=num_samples, freq="h")
hours = timestamps.hour
is_rain = np.random.binomial(1, 0.15, num_samples)    # 15% xác suất có mưa
is_event = np.random.binomial(1, 0.05, num_samples)   # 5% xác suất có sự kiện lớn

# Target biến thiên phụ thuộc vào Features (Base Demand ~ 15.0)
# Biểu thức thực tế: log(Demand) = log(15) + (Rush_Hour)*0.916 + (Rain)*0.262 + (Event)*1.098 
# Exponential để đưa về số đếm
demand = []
for h, r, e in zip(hours, is_rain, is_event):
    is_rush_hour = 1 if ((7 <= h <= 9) or (17 <= h <= 19)) else 0
    # Base là 15 + noise
    base_mu = np.log(15.0) + (is_rush_hour * np.log(2.5)) + (r * np.log(1.3)) + (e * np.log(3.0))
    # Sinh lượng cuốc xe thực tế bằng phân phối Poisson
    actual_rides = np.random.poisson(lam=np.exp(base_mu))
    demand.append(actual_rides)

df = pd.DataFrame({
    'timestamp': timestamps,
    'hour': hours,
    'is_rush_hour': [1 if ((h >= 7 and h <= 9) or (h >= 17 and h <= 19)) else 0 for h in hours],
    'is_rain': is_rain,
    'is_event': is_event,
    'total_demand': demand
})

csv_path = "taxi_demand_dataset.csv"
df.to_csv(csv_path, index=False)
print(f"Đã lưu thành công tập dữ liệu mẫu: {csv_path} ({num_samples} dòng)\n")

# ==========================================
# PHẦN 2: HUẤN LUYỆN MÔ HÌNH VÀ TRÍCH XUẤT TRỌNG SỐ
# ==========================================
print("Đang huấn luyện mô hình Máy Học Poisson Regression...")

X = df[['is_rush_hour', 'is_rain', 'is_event']]
y = df['total_demand']

# Chia tập Train/Test
X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2, random_state=42)

# Poisson Regression phù hợp với dự đoán Count Data (số nguyên, lượng chuyến xe)
model = PoissonRegressor(alpha=1e-4, max_iter=300)
model.fit(X_train, y_train)

# Đánh giá cơ bản
y_pred = model.predict(X_test)
score = mean_poisson_deviance(y_test, y_pred)
print(f"Poisson Deviance Score (Càng thấp càng tốt): {score:.4f}\n")

# ==========================================
# PHẦN 3: XUẤT THAM SỐ TRANSLATE QUA NATIVE JAVA VÀ TẠO FILE JSON
# ==========================================
print("=== THAM SỐ GẮN VÀO JAVA (DemandPredictionService.java) ===")

# Base Intercept (Khi exp lên, nó chính là Base Demand ban đầu)
base_demand = np.exp(model.intercept_)
print(f"-> Base Intercept: {base_demand:.2f} (Gần đúng 15.0)")

# Multipliers: Tác động của từng Featue (Lấy exp của coef_)
multipliers = np.exp(model.coef_)
print(f"-> Rush Hour Multiplier: x{multipliers[0]:.2f}")
print(f"-> Rain Multiplier: x{multipliers[1]:.2f}")
print(f"-> Event Multiplier: x{multipliers[2]:.2f}")

print("\nĐang xuất cấu hình mô hình ra file model_weights.json để Java tải tự động...")
import json
import os

model_weights = {
    "baseIntercept": round(float(base_demand), 3),
    "rushHourMultiplier": round(float(multipliers[0]), 3),
    "rainMultiplier": round(float(multipliers[1]), 3),
    "eventMultiplier": round(float(multipliers[2]), 3)
}

curr_dir = os.path.dirname(os.path.abspath(__file__))
json_path = os.path.join(curr_dir, "model_weights.json")

with open(json_path, 'w', encoding='utf-8') as f:
    json.dump(model_weights, f, indent=4)

print(f"Đã xuất file thành công tại {json_path}")
