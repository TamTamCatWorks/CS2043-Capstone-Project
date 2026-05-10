package org.tamtamcatworks.auction.model.item;

import org.tamtamcatworks.auction.model.BaseEntity;
import org.tamtamcatworks.auction.model.user.User;
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
    @Column(nullable = false)
    private ItemCondition condition;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private User seller;

    @Column(nullable = false)
    private String imageUrl;

    @Column(nullable = false)
    private LocalDateTime listedAt;

    protected Item(
            String name,
            String description,
            double startingPrice,
            ItemCondition condition,
            String imageUrl,
            User seller) {
        super(); 
        setName(name);
        setStartingPrice(startingPrice);
        setDescription(description);
        setCondition(condition);
        setImageUrl(imageUrl);
        this.seller = seller;
        this.listedAt = java.time.LocalDateTime.now();
    }

    protected Item() { };

    public abstract String getSpecificInfo();

    @Override
    public String toString() {
        String typeName = this.getClass().getSimpleName();

        return "[" + typeName + "] " + name
                + " | ID: " + getId()
                + " | Mô tả: " + description
                + " | Tình trạng: " + condition.getDisplayName()
                + " | Giá khởi: " + String.format("%,.0f VNĐ", startingPrice)
                + " | Người bán: " + seller
                + " | " + getSpecificInfo();
    }

    public String getName() { return name; }
    public String getDescription() { return description; }
    public double getStartingPrice() { return startingPrice; }
    public ItemCondition getCondition() { return condition; }
    public User getSeller() { return seller;}
    public String getImageUrl() { return imageUrl; }
    public LocalDateTime getListedAt() { return listedAt; }

    public void setName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Tên sản phẩm không được để trống.");
        }
        this.name = name.trim();
    }

    public void setDescription(String description) {

        if (description == null) {

            this.description = "";

            return;
        }

        this.description = description.trim();
    }

    public void setStartingPrice(double startingPrice) {
        if (startingPrice <= 0) {
            throw new IllegalArgumentException("Giá khởi điểm phải lớn hơn 0.");
        }
        this.startingPrice = startingPrice;
    }

    public void setCondition(ItemCondition condition) {

        if (condition == null) {

            throw new IllegalArgumentException("Condition is required.");
        }

        this.condition = condition;
    }
    public void setImageUrl(String imageUrl) {

        if (imageUrl == null || imageUrl.isBlank()) {

            throw new IllegalArgumentException("Image URL cannot be empty.");
        }

        this.imageUrl = imageUrl.trim();
    }
}
