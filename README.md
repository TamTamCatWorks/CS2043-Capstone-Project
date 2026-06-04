# CS2043-Capstone-Project
Tóm Tắt Báo Cáo — Auction System (CS2043 · TamTamCatWorks)


1. Mô tả bài toán và phạm vi hệ thống

Hệ thống là một nền tảng đấu giá trực tuyến full-stack, thời gian thực, cho phép nhiều người dùng cạnh tranh mua sản phẩm trong một khoảng thời gian xác định. Khác với thương mại giá cố định, giá cuối cùng được quyết định thông qua quá trình đặt giá cạnh tranh giữa các bidder đã đăng ký.

Phạm vi triển khai bao gồm:
- Quản lý người dùng với phân quyền theo vai trò (Buyer, Seller, Admin)
- Quản lý vòng đời phiên đấu giá: tạo → kích hoạt → đặt giá → đóng/huỷ
- Cập nhật thời gian thực qua WebSocket (STOMP over SockJS)
- Đặt giá an toàn với optimistic locking và cơ chế giữ tiền (fund reservation)
- Các tính năng nâng cao: chống snipe, tự động đặt giá (auto-bid), tìm kiếm & phân trang, lưu trữ ảnh với MinIO
- Giao diện desktop JavaFX cho cả người dùng thông thường và quản trị viên


2. Công nghệ sử dụng, môi trường chạy và yêu cầu cài đặt

Thành phần                                      Công nghệ
Backend(Server)                                 Java 21, Spring Boot 3.2.5
Frontend (Client)                               JavaFX 21, AtlantaFX
Cơ sở dữ liệu                                   PostgreSQL 16
Lưu trữ ảnh                                     MinIO(S3-compatible)
Build tool                                      Apache Maven
Container                                       Docker & Docker Compose
CI/CD                                           GitHub Actions
Test                                            JUnit5, Mockito

Yêu cầu cài đặt trước khi chạy:
- Java 21 (khuyến nghị dùng Eclipse Temurin 21)
- Apache Maven 3.9+
- Docker & Docker Compose (để chạy PostgreSQL + MinIO)

Kiểm tra phiên bản trên terminal (Linux/macOS/Windows):
    java -version
    mvn -version
    docker --version
    docker compose version


3. Cấu trúc các module chính

Dự án sử dụng Maven multi-module, tổ chức theo thứ tự phụ thuộc từ dưới lên:
CS2043-Capstone-Project/
└── auction-system/
    ├── pom.xml                  ← Parent POM
    ├── docker-compose.yml       ← PostgreSQL + MinIO
    ├── shared/                  ← DTOs dùng chung (request/response records)
    ├── model/                   ← JPA entities (Auction, User, Item, ...)
    ├── persist/                 ← Spring Data repositories
    ├── service/                 ← Business logic (BidService, AuctionService, ...)
    ├── api/                     ← Spring Boot REST API + WebSocket
    └── client/                  ← JavaFX desktop application

Chiều phụ thuộc module: shared ← model ← persist ← service ← api ← client


4. Câu lệnh dòng lệnh để chạy chương trình
(Tất cả lệnh dưới đây chạy được trên Linux, macOS và Windows (dùng terminal tương ứng: bash/zsh/PowerShell).)

Bước 1 — Clone dự án:
    git clone https://github.com/<your-org>/CS2043-Capstone-Project.git
    cd CS2043-Capstone-Project/auction-system

Bước 2 — Khởi động PostgreSQL và MinIO bằng Docker Compose:
    docker compose up -d

    Lệnh này sẽ khởi động:
    - PostgreSQL 16 tại cổng 5432, database tamtamcatworks
    - MinIO tại cổng 9000 (API) và 9001 (Console)
    - Init container tự động tạo bucket auction-images với quyền truy cập public

    Kiểm tra container đã chạy:
        docker compose ps

Bước 3 — Build toàn bộ dự án:
    # Linux / macOS
    mvn clean package -DskipTests

    # Windows (PowerShell hoặc Command Prompt)
    mvn clean package "-DskipTests"

Bước 4 — Chạy Server (API):
    # Linux / macOS
    java -jar api/target/api-*.jar

    # Windows (PowerShell)
    java -jar (Get-Item api/target/api-*.jar).FullName

    # Windows (Command Prompt) — cần chỉ tên file cụ thể, ví dụ:
    java -jar api/target/api-1.0.1-SNAPSHOT.jar

Bước 5 — Chạy Client (JavaFX):
    # Linux / macOS
    mvn -pl client javafx:run

    # Windows (PowerShell hoặc Command Prompt)
    mvn -pl client javafx:run

(Lưu ý trên Linux: Nếu chưa có môi trường đồ hoạ (ví dụ server không có GUI), cần cài thêm libgtk và các thư viện JavaFX native. Trên máy desktop thông thường (Ubuntu, Fedora) thì chạy bình thường.)


5. Hướng dẫn khởi động theo thứ tự cụ thể

Cần khởi động đúng thứ tự sau để tránh lỗi kết nối:
[1] Docker Compose   →   [2] API Server   →   [3] JavaFX Client

Bước                    Lệnh                                Chờ đến khi
1. Hạ tầng              docker compose up -d                Cả hai container postgres và minio ở trạng thái healthy / running
2. Server               java -jar api/target/api-*.jar      Console in ra Started Application hoặc Tomcat started on port 8080
3. Client               mvn -pl client javafx:run           Cửa sổ đăng nhập JavaFX hiện lên

Khi tắt, nên dừng theo thứ tự ngược lại: Client → Server → Docker.

Để tắt Docker sau khi dùng xong: 
    docker compose down

Nếu muốn xoá luôn dữ liệu (database + ảnh MinIO):
    docker compose down -v


6. Danh sách chức năng đã hoàn thành

    6.1. User/Item Management (1p)

        - User Management
        - Auction Item Management

    6.2. Auction Functionalities (1p)

        - Bidding
        - Auction Closure & Management with Spring Scheduler
        - Global Exception Handling with @RestControllerAdvice
        - GUI with javaFX & atlantaFX

    6.3. Concurrency & Realtime Update (1.5p)

        - Concurrent Bidding Handling (Thanks to Spring) (1p)
        - Realtime Update (Observer/Websockets) (0.5p)

    6.4. Further Functionalities (0.5 per), max (1.5p)

        - Admin Panel
        - Anti-sniping
        - Auto-bidding
        - Bid History Visualization with Line chart
        - Search & Pagination
        - Object Storage with Minio


7. Link báo cáo PDF và video demo

    - Link báo cáo PDF:
        ...

    - Link video demo:
        https://drive.google.com/drive/folders/1X-gnQJJQ-8LMUYonsePNU4ffjVp54QlD