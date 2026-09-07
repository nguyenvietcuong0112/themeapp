# 📖 TÀI LIỆU HƯỚNG DẪN SỬ DỤNG GOLDIE (TỪ A ĐẾN Z)
> **Tự động hoá tạo ảnh chụp màn hình (Screenshots) và Video Preview cho Google Play Store & Apple App Store.**

---

## 📌 MỤC LỤC
1. [Goldie là gì?](#1-goldie-là-gì)
2. [Yêu cầu môi trường & Cài đặt ban đầu](#2-yêu-cầu-môi-trường--cài-đặt-ban-đầu)
3. [Cấu trúc thư mục chuẩn trong dự án](#3-cấu-trúc-thư-mục-chuẩn-trong-dự-án)
4. [Hướng dẫn chi tiết File cấu hình `goldie.config.ts`](#4-hướng-dẫn-chi-tiết-file-cấu-hình-goldieconfigts)
5. [Hướng dẫn viết kịch bản điều hướng `.argent/flows/*.yaml`](#5-hướng-dẫn-viết-kịch-bản-điều-hướng-argentflowsyaml)
6. [Quy trình chạy lệnh từng bước (Workflow)](#6-quy-trình-chạy-lệnh-từng-bước-workflow)
7. [Bảng tra cứu Layout & Bố cục (Templates)](#7-bảng-tra-cứu-layout--bố-cục-templates)
8. [Hướng dẫn sử dụng Web Studio trực quan](#8-hướng-dẫn-sử-dụng-web-studio-trực-quan)
9. [Xử lý các lỗi thường gặp (Troubleshooting)](#9-xử-lý-các-lỗi-thường-gặp-troubleshooting)
10. [Bảng tra cứu lệnh nhanh (Cheatsheet)](#10-bảng-tra-cứu-lệnh-nhanh-cheatsheet)

---

## 1. Goldie là gì?

Trước đây, để chuẩn bị một bộ ảnh chụp màn hình đăng lên Google Play Store hoặc App Store, bạn phải:
1. Mở ứng dụng trên máy, chụp thủ công từng màn hình.
2. Đưa ảnh vào Photoshop hoặc Figma.
3. Tìm mockup khung điện thoại (bezel), lồng ảnh vào khung.
4. Nghĩ tiêu đề marketing, vẽ nền gradient, căn chỉnh đúng tỷ lệ kích thước mà cửa hàng yêu cầu.

**Goldie tự động hoá 100% công việc này:**
- Tự điều hướng ứng dụng và chụp ảnh màn hình thô (`capture`).
- Tự lồng khung điện thoại (Pixel 10 Pro cho Android, iPhone 17 Pro cho iOS), tô nền gradient, chèn chữ quảng cáo (`frame`).
- Mở một trang web Studio nội bộ mô phỏng trang Play Store/App Store thật để bạn xem trước, chỉnh sửa thiết kế trực tiếp và tải file zip ảnh về (`studio`).

---

## 2. Yêu cầu môi trường & Cài đặt ban đầu

### 2.1. Cài đặt các công cụ cần thiết
1. **Node.js**: Phiên bản 20 trở lên (`node -v`).
2. **Goldie CLI**: Đã cài đặt toàn cục hoặc chạy qua `npx`:
   ```bash
   npm install -g goldie
   # Hoặc kiểm tra:
   goldie version
   ```
3. **FFmpeg**: Cần thiết để xử lý định dạng ảnh (loại bỏ alpha channel theo chuẩn Google Play) và render video preview.
   - **macOS**: `brew install ffmpeg`
   - **Windows**: `winget install ffmpeg`
   - **Linux**: `sudo apt install ffmpeg`
4. **Android SDK / ADB**: Máy đã cài Android SDK và lệnh `adb` có sẵn trong terminal (`adb devices`).

---

## 3. Cấu trúc thư mục chuẩn trong dự án

Để dự án của bạn hoạt động được với Goldie, bạn chỉ cần cấu hình 2 thư mục sau ở thư mục gốc:

```text
my-app/
├── app/
│   └── build/outputs/apk/debug/app-debug.apk   <-- File APK build ra từ Android Studio
│
├── .argent/
│   └── flows/                                  <-- Chứa kịch bản bấm app tự động
│       ├── store-01-themes.yaml
│       ├── store-02-control-center.yaml
│       ├── store-03-icons.yaml
│       └── store-04-widgets.yaml
│
└── goldie/                                     <-- Chứa cấu hình & kết quả xuất ra
    ├── goldie.config.ts                        <-- FILE CẤU HÌNH TRUNG TÂM
    └── out/                                    <-- Thư mục sinh tự động (thêm vào .gitignore)
        ├── raw/                                <-- Ảnh chụp thô từ máy
        ├── screenshots/                        <-- Ảnh hoàn thiện (đã ghép khung & chữ)
        └── web/store.json                      <-- Dữ liệu hiển thị cho trang Studio
```

> **Lưu ý:** Hãy thêm `goldie/out/` vào file `.gitignore` để không đẩy thư mục ảnh build lên Git.

---

## 4. Hướng dẫn chi tiết File cấu hình `goldie.config.ts`

Tệp [`goldie/goldie.config.ts`](file:///Users/cuong/Documents/pheej_studio/themeapp/goldie/goldie.config.ts) quy định toàn bộ nội dung, màu sắc và kịch bản marketing.

### Cấu trúc đầy đủ & Giải thích từng trường:

```ts
const APP_ROOT = "/Users/cuong/Documents/pheej_studio/themeapp";

const config = {
  // 1. Thư mục gốc của dự án
  appRoot: APP_ROOT,

  // 2. Khai báo thông tin ứng dụng Android
  android: {
    appPath: `${APP_ROOT}/app/build/outputs/apk/debug/app-debug.apk`, // Đường dẫn tới file APK
    applicationId: "com.themes.diy.widgets.keyboard.controlcenter",   // Package ID của app
  },

  // 3. Thiết bị nhắm tới
  // "pixel-10-pro": chuẩn Google Play Store (khung Google Pixel)
  // "iphone-6.9": chuẩn Apple App Store (khung iPhone 17 Pro Max)
  devices: ["pixel-10-pro"],

  // 4. Ngôn ngữ & Chế độ sáng/tối
  locales: ["en-US"],
  appearance: "light", // "light" hoặc "dark"

  // 5. Khung viền thiết bị (Bezel finish)
  frame: { variant: "17-pro-orange" }, // "17-pro-silver" | "17-pro-blue" | "17-pro-orange"

  // 6. Phong cách giao diện của ảnh (Theme)
  theme: {
    // Nền gradient dạng CSS (có thể dùng mã màu đơn sắc hoặc gradient)
    background: "linear-gradient(160deg, #FFF3ED 0%, #FFF8F5 50%, #FFFFFF 100%)",
    headlineColor: "#1E1E24", // Màu chữ tiêu đề chính
    subheadColor: "#6E7387",  // Màu chữ tiêu đề phụ
    fontFamily: '-apple-system, "SF Pro Display", system-ui, sans-serif',
    copyHeightRatio: 0.22,    // Tỷ lệ chiều cao dành cho vùng chữ (22%)
    deviceWidthRatio: 0.84,   // Tỷ lệ độ rộng khung máy so với khổ ảnh (84%)
    template: "editorial",    // Nhịp bố cục cả dải ảnh: "editorial", "showcase", "magazine", "dynamic"
    layout: "classic",        // Bố cục mặc định cho từng ảnh nếu không chỉ định riêng
  },

  // 7. Thông tin hiển thị trên trang Store mô phỏng (Studio)
  store: {
    name: "Themes: Control Center, Icons",
    subtitle: { "en-US": "Cute Themes, DIY Widgets & Icons" },
    developer: "Pheej Studio",
    category: "Personalization",
    rating: 4.8,
    ratingCount: "3.5K Ratings",
    ageRating: "3+",
    price: "Free",
    description: {
      "en-US": "Personalize your Android device with cute aesthetic themes, dynamic control center, custom icons, and handy widgets. Express your unique style effortlessly!",
    },
  },

  // 8. Danh sách các màn hình cần tạo (Scenes)
  scenes: [
    {
      kind: "screenshot",
      id: "themes",                                                     // Mã định danh của cảnh
      flow: "store-01-themes",                                          // Tên file trong .argent/flows/
      headline: { "en-US": "Aesthetic Themes & Wallpapers" },           // Tiêu đề nổi bật trên ảnh
      subhead: { "en-US": "Thousands of curated themes to match your mood." }, // Mô tả phụ bên dưới
    },
    {
      kind: "screenshot",
      id: "control-center",
      flow: "store-02-control-center",
      headline: { "en-US": "Custom Control Center" },
      subhead: { "en-US": "Quick access to music, volume, wifi and shortcuts." },
    },
    {
      kind: "screenshot",
      id: "icons",
      flow: "store-03-icons",
      headline: { "en-US": "Unique App Icon Packs" },
      subhead: { "en-US": "Transform your home screen with matching cute icons." },
    },
    {
      kind: "screenshot",
      id: "widgets",
      flow: "store-04-widgets",
      headline: { "en-US": "Handy DIY Widgets" },
      subhead: { "en-US": "Decorate your screen with clock, calendar and weather." },
    },
  ],
};

export default config;
```

---

## 5. Hướng dẫn viết kịch bản điều hướng `.argent/flows/*.yaml`

Các file kịch bản điều hướng nằm trong thư mục `.argent/flows/`. Mỗi file đại diện cho chuỗi thao tác của người dùng để đến màn hình cần chụp.

### 5.1. Cú pháp cơ bản của một Flow
```yaml
steps:
  - echo: Ghi chú giải thích bước này làm gì (in ra terminal khi chạy)
  - launch: com.themes.diy.widgets.keyboard.controlcenter   # Mở app theo package name
  - await:
      visible:
        id: navItemControl                                 # Chờ đến khi thấy View ID này
  - tap:
      id: navItemControl                                   # Nhấn vào View ID đó
  - await:
      idle: true                                           # Chờ hiệu ứng chuyển cảnh dừng hẳn
  - wait: 1200                                             # Dừng thêm 1.2 giây (1200ms) để ổn định giao diện
```

### 5.2. Các câu lệnh thông dụng trong Flow:
- **`launch: <package_id>`**: Khởi chạy ứng dụng.
- **`tap: { id: <view_id> }`**: Nhấn vào một View theo ID (Khuyên dùng nhất vì không bị lệch toạ độ).
- **`tap: { text: "Cài đặt" }`**: Nhấn vào nút theo dòng chữ hiển thị.
- **`tap: { x: 0.5, y: 0.8 }`**: Nhấn theo toạ độ chuẩn hoá (từ 0.0 đến 1.0) khi View không có ID hay chữ.
- **`await: { visible: { id: <id> } }`**: Chờ cho tới khi phần tử xuất hiện trên màn hình.
- **`await: { hidden: { id: <id> } }`**: Chờ cho tới khi phần tử biến mất (ví dụ loading dialog).
- **`await: { idle: true }`**: Chờ ứng dụng đứng yên (hết animation).
- **`wait: <miligiây>`**: Dừng cưỡng bức một khoảng thời gian trước khi chụp.

> **Mẹo quan trọng:** Luôn kết thúc flow bằng `await: { idle: true }`. Bạn không cần gọi lệnh chụp ảnh trong file yaml vì Goldie sẽ tự động chụp màn hình ở bước cuối cùng của flow.

---

## 6. Quy trình chạy lệnh từng bước (Workflow)

Mỗi lần chạy lệnh Goldie, hãy truyền biến môi trường `GOLDIE_CONFIG`:

```bash
# Định nghĩa biến môi trường cho nhanh
export GOLDIE_CONFIG=goldie/goldie.config.ts
```

### Bước 6.1. Kiểm tra môi trường (`doctor`)
Kiểm tra xem file APK, kết nối ADB, FFmpeg và các file YAML đã hợp lệ chưa:
```bash
GOLDIE_CONFIG=goldie/goldie.config.ts goldie doctor
```

### Bước 6.2. Chụp ảnh màn hình thô (`capture`)
Goldie sẽ mở app trên máy thật/máy ảo, chạy các kịch bản YAML và lưu ảnh thô vào `goldie/out/raw/`:
```bash
GOLDIE_CONFIG=goldie/goldie.config.ts goldie capture
```

### Bước 6.3. Ghép khung viền, nền và tiêu đề (`frame`)
Lấy các ảnh thô trong `out/raw/`, ghép viền điện thoại Pixel/iPhone, vẽ nền gradient, in tiêu đề marketing và lưu vào `goldie/out/screenshots/`:
```bash
GOLDIE_CONFIG=goldie/goldie.config.ts goldie frame
```

### Bước 6.4. Xác minh chuẩn kích thước Store (`verify`)
Kiểm tra xem toàn bộ ảnh xuất ra có đúng độ phân giải và không chứa alpha channel (theo yêu cầu của Google Play):
```bash
GOLDIE_CONFIG=goldie/goldie.config.ts goldie verify
```

### Bước 6.5. Mở Web Studio xem trước (`studio`)
Khởi chạy giao diện mô phỏng store tại trình duyệt:
```bash
GOLDIE_CONFIG=goldie/goldie.config.ts goldie studio
```
Truy cập: **[http://localhost:4321](http://localhost:4321)**

---

## 7. Bảng tra cứu Layout & Bố cục (Templates)

Bạn có thể thay đổi nhịp điệu của dải ảnh trên Store thông qua thuộc tính `template` hoặc `layout` trong `goldie.config.ts`:

### 7.1. Các Template có sẵn (`theme.template`)
| Template | Trình tự các bố cục | Phù hợp cho |
| :--- | :--- | :--- |
| `editorial` *(Mặc định)* | panorama -> hero -> offset -> minimal -> tilt | Bộ ảnh chuẩn cao cấp, bắt mắt |
| `showcase` | hero -> tilt -> duo -> tilt-right -> minimal | Nổi bật tính năng cốt lõi |
| `magazine` | offset -> copy-below -> tilt-right -> hero -> minimal | Phong cách thanh lịch, nhẹ nhàng |
| `dynamic` | tilt -> duo-tilt -> panorama -> minimal -> tilt-right | Ứng dụng trẻ trung, sinh động |

### 7.2. Các kiểu Layout cho từng ảnh (`layout`)
- **`classic`**: Tiêu đề nằm trên ở giữa, điện thoại đứng thẳng ở giữa.
- **`hero`**: Tiêu đề nằm trên, điện thoại phóng to tràn viền dưới.
- **`tilt`**: Điện thoại nghiêng chéo sang một góc nghệ thuật.
- **`offset`**: Tiêu đề căn trái, điện thoại lệch sang góc phải dưới.
- **`minimal`**: Chỉ có điện thoại lớn ở giữa, không có chữ tiêu đề (tạo khoảng thở).
- **`panorama`**: Một chiếc điện thoại nghiêng nối liền qua 2 tấm ảnh cạnh nhau (ghép đôi).

---

## 8. Hướng dẫn sử dụng Web Studio trực quan

Khi bạn chạy `goldie studio` và mở **`http://localhost:4321`**, bạn có:

1. **Giao diện Store mô phỏng (Storefront Preview)**:
   - Thấy ảnh hiển thị chính xác như khi người dùng lướt xem app của bạn trên Google Play Store / App Store.
2. **Bảng tinh chỉnh thiết kế (Design Panel bên phải)**:
   - **Background**: Chọn màu nền khác hoặc đổi góc gradient trực tiếp.
   - **Template & Layout**: Thử đổi sang `showcase`, `magazine` hoặc nghiêng máy chỉ với 1 cú click.
   - **Typography**: Đổi font chữ (`SF Pro`, `Montserrat`, `Lato`, `Merriweather`,...).
   - **Screen Only**: Tắt viền máy nếu muốn hiển thị màn hình phẳng bóng đổ.
3. **Nút Export**:
   - Nhấn **Export** ở góc trên để tải về ngay file `.zip` chứa toàn bộ ảnh độ phân giải cao sẵn sàng tải thẳng lên **Google Play Console**.

---

## 9. Xử lý các lỗi thường gặp (Troubleshooting)

### 9.1. Lỗi `spawn ffmpeg ENOENT`
- **Nguyên nhân:** Máy chưa cài `ffmpeg` hoặc chưa nhận biến môi trường PATH.
- **Cách xử lý:** Chạy `brew install ffmpeg` trên terminal.

### 9.2. Lỗi `Cannot use 'in' operator to search for 'variant' in undefined`
- **Nguyên nhân:** Thiếu trường `frame: { variant: "17-pro-orange" }` trong `goldie.config.ts`.
- **Cách xử lý:** Bổ sung trường `frame` vào config.

### 9.3. Lỗi `FAIL video-watermark ENABLED`
- **Nguyên nhân:** Driver argent bật cờ watermark cho video.
- **Cách xử lý:** Chạy lệnh:
  ```bash
  /usr/local/lib/node_modules/goldie/node_modules/.bin/argent disable video-watermark
  ```

### 9.4. Muốn đổi lại tiêu đề hoặc màu nền thì làm thế nào?
- Bạn **không cần chụp lại ảnh**! Chỉ cần:
  1. Mở file [`goldie/goldie.config.ts`](file:///Users/cuong/Documents/pheej_studio/themeapp/goldie/goldie.config.ts) sửa chữ hoặc mã màu.
  2. Chạy lại lệnh ghép ảnh:
     ```bash
     GOLDIE_CONFIG=goldie/goldie.config.ts goldie frame
     ```
  3. Mở web `http://localhost:4321` tải lại trang là thấy ngay ảnh mới.

---

## 10. Bảng tra cứu lệnh nhanh (Cheatsheet)

| Nhu cầu | Lệnh chạy |
| :--- | :--- |
| **Kiểm tra môi trường** | `GOLDIE_CONFIG=goldie/goldie.config.ts goldie doctor` |
| **Chụp lại toàn bộ màn hình** | `GOLDIE_CONFIG=goldie/goldie.config.ts goldie capture` |
| **Ghép lại khung & chữ sau khi sửa config** | `GOLDIE_CONFIG=goldie/goldie.config.ts goldie frame` |
| **Kiểm tra kích thước chuẩn Store** | `GOLDIE_CONFIG=goldie/goldie.config.ts goldie verify` |
| **Mở Web Studio để xem & tải ảnh zip** | `GOLDIE_CONFIG=goldie/goldie.config.ts goldie studio` |
| **Chạy toàn bộ quy trình từ đầu đến cuối** | `GOLDIE_CONFIG=goldie/goldie.config.ts goldie all` |
