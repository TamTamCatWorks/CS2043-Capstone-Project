package org.tamtamcatworks.auction;
/**
 * Singleton class quản lý tất cả các phiên đấu giá trong hệ thống.
 *
 * <p>SINGLETON PATTERN (Mẫu Singleton):
 * - Chỉ có một instance của AuctionManager tồn tại trong toàn bộ ứng dụng
 * - Đảm bảo tất cả các phần của hệ thống truy cập cùng một manager
 * - Quản lý tập trung tất cả auction, user, bid...
 *
 * <p>DOUBLE-CHECKED LOCKING:
 * - Sử dụng volatile để đảm bảo visibility giữa các thread
 * - Check null 2 lần để tránh synchronized overhead sau khi instance đã tạo
 * - Thread-safe initialization mà không cần synchronized mọi lần gọi
 *
 * <p>WHY SINGLETON:
 * - AuctionManager giữ state toàn cục (map của auctions, users...)
 * - Không nên có nhiều manager → gây inconsistency
 * - Dễ quản lý lifecycle và resources
 *
 * <p>FUTURE EXTENSIONS:
 * - Thêm Map<String, Auction> auctions để lưu tất cả phiên
 * - Thêm Map<String, User> users để lưu tất cả user
 * - Thêm các method để tạo, tìm, xóa auction
 *
 * @author Nguyen Hoang Vu
 * @version 1.0
 */
public class AuctionManager {

    /**
     * Instance duy nhất của AuctionManager.
     *
     * <p>TẠI SAO VOLATILE:
     * - Đảm bảo visibility giữa các thread
     * - Khi một thread khởi tạo instance, các thread khác sẽ thấy ngay lập tức
     * - Tránh vấn đề "partially constructed object" do instruction reordering
     *
     * <p>NOTE:
     * - volatile đảm bảo happens-before relationship
     * - Bắt buộc cho double-checked locking pattern
     */
    private static volatile AuctionManager instance;

    /**
     * Private constructor.
     *
     * <p>TẠI SAO PRIVATE:
     * - Ngăn chặn việc tạo instance từ bên ngoài class
     * - Chỉ có getInstance() mới có thể tạo instance
     * - Đảm bảo singleton property
     */
    private AuctionManager() {
        // Khởi tạo resources nếu cần (DB connection, cache...)
    }

    /**
     * Lấy instance duy nhất của AuctionManager.
     *
     * <p>DOUBLE-CHECKED LOCKING LOGIC:
     * 1. First check (instance == null):
     *    - Nhanh, không synchronized
     *    - Tránh overhead khi instance đã tồn tại
     * 2. Synchronized block:
     *    - Chỉ một thread có thể tạo instance tại một thời điểm
     *    - Đảm bảo thread-safe initialization
     * 3. Second check (instance == null):
     *    - Trong synchronized block
     *    - Tránh tạo duplicate instance khi nhiều thread pass first check cùng lúc
     *
     * <p>PERFORMANCE:
     * - Chỉ synchronized lần đầu tiên
     * - Các lần sau chỉ có first check (rất nhanh)
     *
     * @return instance duy nhất của AuctionManager
     */
    public static AuctionManager getInstance() {
        if (instance == null) {  // First check: không synchronized
            synchronized(AuctionManager.class) {  // Lock class-level
                if(instance == null) {  // Second check: trong synchronized
                    instance = new AuctionManager();
                }
            }
        }
        return instance;
    }
}
