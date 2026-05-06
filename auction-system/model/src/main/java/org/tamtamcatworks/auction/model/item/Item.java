package org.tamtamcatworks.auction.model.item;

import org.tamtamcatworks.auction.model.BaseEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "items")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "item_type")
public abstract class Item extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private double startingPrice;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, insertable = false, updatable = false)
    private final ItemType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ItemCondition condition;

    @Column(nullable = false)
    private String sellerId;

    @Column(nullable = false)
    private String imageUrl;

    @Column(nullable = false)
    private LocalDateTime listedAt;


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
    public String toString() {
        return "[" + type.getDisplayName() + "] " + name
                + " | ID: " + getId()
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
