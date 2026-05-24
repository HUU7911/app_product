# Ứng Dụng Quản Lý Sản Phẩm Bằng Java Swing (JDK 25)

# Dowload
```bash
git clone https://github.com/HUU7911/app_product.git
```
# Chạy dự án

```bash
javac Main.java
java Main
```
## Giới Thiệu

Đây là ứng dụng quản lý sản phẩm được phát triển bằng **Java Swing** chạy trên **JDK 25**.  
Ứng dụng được xây dựng theo hướng Desktop Application với giao diện trực quan, dễ sử dụng và có khả năng xử lý dữ liệu lớn.

Dự án phù hợp cho:
- Sinh viên học Java Desktop
- Bài tập môn Java Swing
- Thực hành CRUD
- Thực hành xử lý ngoại lệ và tìm kiếm dữ liệu

---

# Công Nghệ Sử Dụng

| Công Nghệ | Mô Tả |
|---|---|
| Java Swing | Xây dựng giao diện |
| JDK 25 | Runtime và compiler |
| JTable | Hiển thị dữ liệu dạng bảng |
| OOP | Thiết kế hướng đối tượng |
| Exception Handling | Xử lý lỗi |

---

# Yêu Cầu Chức Năng

## 1. Giao Diện Đẹp Và Dễ Nhìn

Ứng dụng phải có:
- Giao diện hiện đại
- Màu sắc hài hòa
- Các nút bấm có màu nổi bật
- Chữ và nền có độ tương phản cao

### Yêu cầu hiển thị bảng:
- Sản phẩm phải hiển thị bằng `JTable`
- Có tiêu đề cho từng cột
- Mỗi thuộc tính nằm trong ô riêng biệt
- Màu phần tiêu đề khác với phần dữ liệu

Ví dụ các cột:
- ID
- Tên sản phẩm
- Danh mục
- Giá
- Số lượng
- Trạng thái

---

## 2. Chức Năng Tìm Kiếm

Tìm kiếm phải hoạt động theo nguyên tắc:

> Chỉ cần nhập một từ liên quan tới sản phẩm thì sản phẩm phải được hiển thị.

Ví dụ:
- Nhập `"iphone"` → hiển thị tất cả sản phẩm chứa iphone
- Nhập `"pro"` → hiển thị các sản phẩm có chữ pro
- Nhập `"samsung"` → hiển thị các sản phẩm samsung

### Yêu cầu:
- Không phân biệt chữ hoa chữ thường
- Tìm kiếm realtime khi nhập
- Có thể tìm theo:
  - Tên sản phẩm
  - Danh mục
  - Trạng thái

---

## 3. Kiểm Thử Với Dữ Liệu Lớn

Ứng dụng phải được khởi tạo sẵn:

# 100 sản phẩm mẫu

Mục đích:
- Kiểm tra hiệu năng JTable
- Kiểm tra tốc độ tìm kiếm
- Kiểm tra khả năng xử lý dữ liệu

### Dữ liệu mẫu gồm:
- Điện thoại
- Laptop
- Phụ kiện
- Tablet
- Smart Watch

---

## 4. Xử Lý Ngoại Lệ

Toàn bộ logic nghiệp vụ phải được xử lý bằng Exception Handling.

### Mục tiêu:
Không để chương trình bị crash khi người dùng thao tác sai.

### Các trường hợp cần xử lý:
- Nhập sai dữ liệu
- Không nhập dữ liệu
- Giá âm
- Số lượng âm
- Ký tự không hợp lệ
- Không chọn dòng khi sửa/xóa
- Lỗi tìm kiếm

### Ví dụ:
```java
try {
    double price = Double.parseDouble(txtPrice.getText());

    if(price < 0){
        throw new IllegalArgumentException("Giá không được âm");
    }

} catch (NumberFormatException e){
    JOptionPane.showMessageDialog(null,
        "Giá phải là số");
}
