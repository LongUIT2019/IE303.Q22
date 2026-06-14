# Hướng dẫn khởi tạo và thiết lập Firebase Firestore Database

Khi bạn đang ở link (Firebase Console > Firestore Database):
`https://console.firebase.google.com/u/2/project/dqn-ee89b/firestore/databases/-default-/data`

Dưới đây là các thao tác chi tiết:

## Bước 1: Khởi tạo Database (Nếu chưa tạo)
1. Nếu màn hình hiện ra chữ **"Create database"** (Tạo cơ sở dữ liệu), hãy click vào nút đó.
2. **Quy tắc bảo mật (Security rules)**: Chọn **Start in test mode** (Bắt đầu ở chế độ thử nghiệm). Chế độ này cài đặt sẵn rules cho phép đọc/ghi thoải mái trong 30 ngày (rất phù hợp để test local). Click **Next**.
3. **Vị trí (Location)**: Chọn `asia-southeast1` (Singapore) hoặc `asia-southeast2` (Jakarta) để có tốc độ kết nối nhanh nhất về Việt Nam. 
4. Click **Tiếp tục/Enable** và chờ Firebase thiết lập database.

## Bước 2: Xem và kiểm tra dữ liệu
Khi tạo xong, sẽ thấy tab **Data (Dữ liệu)** trống trơn chưa có Collection (Bộ sưu tập) nào. 

Hệ thống Spring Boot Java đang sử dụng **Firebase Admin SDK** (thông qua file JSON cấu hình). Code sẽ **tự động tạo ra các collection** khi có dữ liệu được ghi vào.

Khi ứng dụng chạy, các thao tác sau sẽ sinh ra dữ liệu tự động trên Firebase:
* Khi Hành khách (Passenger) đặt xe -> Tạo ra collection `rides`.
* Khi Tài xế (Driver) nhận cuốc -> Update collection `rides`.
* Khi AI tính toán tạo Heatmap -> Có thể lưu vào `demand_predictions` (nếu code có lưu).

## Bước 3: Thử nghiệm tạo dữ liệu bằng tay (Không bắt buộc)
1. Click **+ Start collection**.
2. **Collection ID**: nhập `rides` -> Click **Next**.
3. **Document ID**: click dòng chữ "Auto-id" để nó tự sinh ra một ID ngẫu nhiên.
4. Ở phần điền Field (Trường), nhập các cột ví dụ:
   - Field: `customerName` | Type: `string` | Value: `Nguyễn Văn A`
   - Field: `pickupLat` | Type: `number` | Value: `10.8`
   - Field: `pickupLng` | Type: `number` | Value: `106.6`
   - Field: `status` | Type: `string` | Value: `REQUESTED`
5. Click **Save**.

Ngay sau khi Save, quay về trang **Admin Dashboard** (`http://localhost:8080/admin`) sẽ thấy **Nguyễn Văn A** xuất hiện vì Spring Boot sẽ query collection `rides` này liên tục!

## Bước 4: Chỉnh sửa Rules (Khi Test Mode hết hạn sau 30 ngày)
Sau 30 ngày, Test Mode sẽ hết hạn và block đọc/ghi nếu truy cập từ trình duyệt. Dù Admin SDK của Spring Boot thì 100% không bị chặn (vì nó chạy với quyền Root Admin), nhưng nếu cần, có thể thiết lập:
1. Chuyển sang tab **Rules**.
2. Đổi rules thành:
```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /{document=**} {
      allow read, write: if true;
    }
  }
}
```
3. Click **Publish**.

