package org.tamtamcatworks.auction.model.item;

import org.tamtamcatworks.auction.model.Entity;
import java.time.LocalDateTime;

/**
 * Lớp trừu tượng đại diện cho mọi sản phẩm đấu giá.
 *
 * <p>INHERITANCE: Item kế thừa Entity → có sẵn id, createdAt, printInfo().
 * Electronics, Art, Vehicle kế thừa Item → có thêm thuộc tính chuyên biệt.
 *
 * <p>ABSTRACTION: Item không thể tạo trực tiếp (abstract).
 * Phải tạo qua ItemFactory để đảm bảo đúng loại.
 *
 * <p>ENCAPSULATION: Tất cả field là private, chỉ thay đổi qua setter
 * có kiểm tra hợp lệ (ví dụ: startingPrice không âm).
 */
public abstract class Item extends Entity {

    // ── Fields ──────────────────────────────────────────────────────────────────

    /** Tên sản phẩm hiển thị trên giao diện. Không được rỗng. */
    private String name;

    /** Mô tả chi tiết: tình trạng, xuất xứ, thông số kỹ thuật... */
    private String description;

    /** Giá khởi điểm — giá thấp nhất để đặt bid đầu tiên. */
    private double startingPrice;

    /** Loại sản phẩm — ELECTRONICS / ART / VEHICLE. */
    private final ItemType type;

    /** Tình trạng vật lý của sản phẩm. */
    private ItemCondition condition;

    /** ID của Seller sở hữu sản phẩm này. */
    private String sellerId;

    /** URL ảnh đại diện sản phẩm (lưu đường dẫn, không lưu file). */
    private String imageUrl;

    /** Thời điểm sản phẩm được thêm vào hệ thống. */
    private LocalDateTime listedAt;

    // ── Constructor ──────────────────────────────────────────────────────────────

    /**
     * Tạo Item mới — gọi từ subclass qua super(...).
     *
     * @param name          tên sản phẩm (không rỗng)
     * @param description   mô tả chi tiết
     * @param startingPrice giá khởi điểm (phải > 0)
     * @param type          loại sản phẩm
     * @param condition     tình trạng
     * @param sellerId      id của người bán
     */
    protected Item(
            String name,
            String description,
            double startingPrice,
            ItemType type,
            ItemCondition condition,
            String sellerId) {
        super(); // gọi Entity() → sinh id, ghi createdAt
        setName(name);
        setStartingPrice(startingPrice);
        this.description = description;
        this.type = type;
        this.condition = condition;
        this.sellerId = sellerId;
        this.listedAt = java.time.LocalDateTime.now();
    }

    /**
     * Constructor load từ database — có id và createdAt sẵn.
     */
    protected Item(
            String id,
            LocalDateTime createdAt,
            String name,
            String description,
            double startingPrice,
            ItemType type,
            ItemCondition condition,
            String sellerId) {
        super(id, createdAt);
        setName(name);
        setStartingPrice(startingPrice);
        this.description = description;
        this.type = type;
        this.condition = condition;
        this.sellerId = sellerId;
        this.listedAt = createdAt;
    }

    // ── Abstract methods ─────────────────────────────────────────────────────────

    /**
     * Trả về tóm tắt thông tin chuyên biệt của từng loại sản phẩm.
     *
     * <p>POLYMORPHISM: Electronics trả về "Brand: Apple | Bảo hành: 12 tháng",
     * Art trả về "Họa sĩ: Picasso | Năm: 1932", v.v.
     *
     * @return chuỗi mô tả đặc trưng của subclass
     */
    public abstract String getSpecificInfo();

    // ── getDisplayInfo — override từ Entity ───────────────────────────────────────

    /**
     * In đầy đủ thông tin sản phẩm ra console.
     * Gọi getSpecificInfo() để hiển thị phần thông tin chuyên biệt.
     */
    @Override
    public String getDisplayInfo() {
        return "[" + type.getDisplayName() + "] " + name
                + " | ID: " + getEntityId()
                + " | Mô tả: " + description
                + " | Tình trạng: " + condition.getDisplayName()
                + " | Giá khởi: " + String.format("%,.0f VNĐ", startingPrice)
                + " | Người bán: " + sellerId
                + " | " + getSpecificInfo();
    }

    // ── Getters ──────────────────────────────────────────────────────────────────

    public String getName() { return name; }
    public String getDescription() { return description; }
    public double getStartingPrice() { return startingPrice; }
    public ItemType getType() { return type; }
    public ItemCondition getCondition() { return condition; }
    public String getSellerId() { return sellerId; }
    public String getImageUrl() { return imageUrl; }
    public LocalDateTime getListedAt() { return listedAt; }

    // ── Setters có kiểm tra hợp lệ ───────────────────────────────────────────────

    /**
     * Cập nhật tên sản phẩm.
     *
     * @param name tên mới (không được null hoặc rỗng)
     * @throws IllegalArgumentException nếu tên rỗng
     */
    public void setName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Tên sản phẩm không được để trống.");
        }
        this.name = name.trim();
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Cập nhật giá khởi điểm.
     *
     * @param startingPrice giá mới (phải > 0)
     * @throws IllegalArgumentException nếu giá <= 0
     */
    public void setStartingPrice(double startingPrice) {
        if (startingPrice <= 0) {
            throw new IllegalArgumentException("Giá khởi điểm phải lớn hơn 0.");
        }
        this.startingPrice = startingPrice;
    }

    public void setCondition(ItemCondition condition) { this.condition = condition; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}
