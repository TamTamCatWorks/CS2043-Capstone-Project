package org.tamtamcatworks.auction.model.item;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;

@Entity
@Table(name = "electronics_items")
@DiscriminatorValue("ELECTRONICS")
@PrimaryKeyJoinColumn(name = "item_id")
public class Electronics extends Item {

    @Column(nullable = false)
    private String brand;

    @Column(nullable = false)
    private String model;

    @Column(nullable = false)
    private int warrantyMonths;

    /**
     * Tạo sản phẩm điện tử mới.
     *
     * name: tên sản phẩm
     * description: mô tả
     * startingPrice: giá khởi điểm
     * condition: tình trạng
     * sellerId: id người bán
     * brand: hãng sản xuất
     * model: tên model
     * warrantyMonths: số tháng bảo hành
     */
    public Electronics(
            String name,
            String description,
            double startingPrice,
            ItemCondition condition,
            String sellerId,
            String brand,
            String model,
            int warrantyMonths) {
        super(name, description, startingPrice, ItemType.ELECTRONICS, condition, sellerId);
        this.brand = brand;
        this.model = model;
        this.warrantyMonths = warrantyMonths;
    }

    /**
     * Trả về thông tin chuyên biệt của Electronics.
     * - Format: "brand | model | warranty"
     */
    @Override
    public String getSpecificInfo() {
        return "Hãng: " + brand
                + " | Model: " + model
                + " | Bảo hành: " + warrantyMonths + " tháng";
    }
}