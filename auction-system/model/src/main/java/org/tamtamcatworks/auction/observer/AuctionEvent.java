package org.tamtamcatworks.auction.observer;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Event được gửi từ Auction đến các Observer trong Observer Pattern.
 *
 * <p>IMMUTABLE CLASS (Lớp bất biến):
 * - Mọi field đều là final → không thể thay đổi sau khi tạo
 * - Class là final → không thể subclass
 * - Tại sao? Để đảm bảo event không bị sửa đổi trong quá trình truyền
 *
 * <p>BUILDER PATTERN (Mẫu Builder):
 * - Sử dụng Builder để tạo event với nhiều optional parameters
 * - Tránh constructor với quá nhiều tham số
 * - Cho phép fluent API: .message("...").data("key", value).build()
 *
 * <p>EVENT STRUCTURE (Cấu trúc event):
 * - type: loại event (BID_PLACED, AUCTION_STARTED...)
 * - auctionId: ID phiên đấu giá phát sinh event
 * - message: mô tả ngắn gọn cho người dùng
 * - timestamp: thời điểm event xảy ra
 * - data: Map chứa thông tin chi tiết (key-value pairs)
 *
 * <p>WHY IMMUTABLE:
 * - Observer không thể sửa event của observer khác
 * - Thread-safe: event có thể truyền giữa các thread
 * - Audit trail: event giữ nguyên trạng thái khi được log
 */
public final class AuctionEvent {

    // ── Fields ──────────────────────────────────────────────────────────────────

    /** Loại event.
     * FINAL → không thể thay đổi loại sau khi tạo. */
    private final AuctionEventType type;

    /** ID của phiên đấu giá phát sinh event.
     * FINAL → event luôn gắn với một auction cụ thể. */
    private final String auctionId;

    /** Thông điệp mô tả event (dùng cho UI/log).
     * FINAL → không thể sửa message. */
    private final String message;

    /** Thời điểm event xảy ra.
     * FINAL → ghi chính xác thời gian. */
    private final LocalDateTime timestamp;

    /** Dữ liệu chi tiết kèm theo event.
     * FINAL → read-only sau khi tạo.
     * UnmodifiableMap → không thể add/remove từ bên ngoài. */
    private final Map<String, Object> data;

    // ── Constructor ─────────────────────────────────────────────────────────────

    /**
     * Private constructor (chỉ Builder gọi được).
     *
     * <p>LOGIC:
     * - Copy data từ Builder sang unmodifiable map
     * - Đảm bảo event không thể thay đổi sau khi tạo
     *
     * @param builder chứa tất cả thông tin để tạo event
     */
    private AuctionEvent(Builder builder) {
        this.type = builder.type;
        this.auctionId = builder.auctionId;
        this.message = builder.message;
        this.timestamp = builder.timestamp;
        // Tạo unmodifiable map để bảo vệ data khỏi sửa đổi
        this.data = Collections.unmodifiableMap(new HashMap<>(builder.data));
    }

    // ── Getters ──────────────────────────────────────────────────────────────────
    // Vì class là immutable, chỉ có getters, không có setters

    public AuctionEventType getType() {
        return type;
    }

    public String getAuctionId() {
        return auctionId;
    }

    public String getMessage() {
        return message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    /**
     * Lấy toàn bộ data map (read-only).
     *
     * <p>TẠI SAO UNMODIFIABLE:
     * - Tránh observer sửa đổi data của event
     * - Đảm bảo integrity của event
     *
     * @return unmodifiable map chứa data
     */
    public Map<String, Object> getData() {
        return data;
    }

    /**
     * Lấy giá trị data theo key với type-safe casting.
     *
     * <p>GENERIC METHOD:
     * - Cho phép specify expected type: getData("price", Double.class)
     * - Tránh manual casting và ClassCastException ở runtime
     *
     * <p>VALIDATION:
     * - Nếu key không tồn tại → return null
     * - Nếu value không đúng type → throw ClassCastException
     *
     * <p>EXAMPLE:
     * Double price = event.getData("finalPrice", Double.class);
     * String winner = event.getData("winnerName", String.class);
     *
     * @param key khóa trong data map
     * @param type class của giá trị mong đợi
     * @return giá trị đã cast, hoặc null nếu key không tồn tại
     * @param <T> type generic
     * @throws ClassCastException nếu value không phải instance của type
     */
    @SuppressWarnings("unchecked")
    public <T> T getData(String key, Class<T> type) {
        Object value = data.get(key);
        if (value == null) {
            return null;
        }
        if (!type.isInstance(value)) {
            throw new ClassCastException("Data key '" + key + "' is not of type " + type.getSimpleName());
        }
        return (T) value;
    }

    // ── Builder Class ───────────────────────────────────────────────────────────

    /**
     * Builder để tạo AuctionEvent.
     *
     * <p>BUILDER PATTERN:
     * - Cho phép tạo event với nhiều optional parameters
     * - Fluent API: chain các method gọi
     * - Code dễ đọc hơn constructor với nhiều tham số
     *
     * <p>EXAMPLE:
     * <pre>
     * new AuctionEvent.Builder(AuctionEventType.BID_PLACED, auctionId)
     *     .message("User đặt giá")
     *     .data("bidAmount", 1000000)
     *     .data("bidderId", "user123")
     *     .build();
     * </pre>
     */
    public static class Builder {

        // Required parameters
        private final AuctionEventType type;
        private final String auctionId;

        // Optional parameters - có default values
        private String message;
        private LocalDateTime timestamp;
        private final Map<String, Object> data;

        /**
         * Tạo Builder với các tham số bắt buộc.
         *
         * @param type loại event
         * @param auctionId ID phiên đấu giá
         */
        public Builder(AuctionEventType type, String auctionId) {
            this.type = type;
            this.auctionId = auctionId;
            this.message = "";  // Default: message rỗng
            this.timestamp = LocalDateTime.now();  // Default: thời gian hiện tại
            this.data = new HashMap<>();  // Default: map rỗng
        }

        /**
         * Thiết lập message cho event.
         *
         * @param message mô tả event
         * @return this (để chain tiếp)
         */
        public Builder message(String message) {
            this.message = message;
            return this;
        }

        /**
         * Thiết lập timestamp cho event.
         *
         * <p>NOTE:
         * - Thường không cần set (default là hiện tại)
         * - Chỉ set khi cần custom timestamp (test, replay...)
         *
         * @param timestamp thời điểm event
         * @return this (để chain tiếp)
         */
        public Builder timestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        /**
         * Thêm key-value vào data map.
         *
         * <p>NOTE:
         * - Có thể gọi nhiều lần để thêm nhiều key-value
         * - Key trùng lặp sẽ ghi đè giá trị cũ
         *
         * @param key khóa
         * @param value giá trị (bất kỳ type nào)
         * @return this (để chain tiếp)
         */
        public Builder data(String key, Object value) {
            this.data.put(key, value);
            return this;
        }

        /**
         * Xây dựng AuctionEvent từ Builder.
         *
         * @return AuctionEvent vừa tạo
         */
        public AuctionEvent build() {
            return new AuctionEvent(this);
        }
    }
}
