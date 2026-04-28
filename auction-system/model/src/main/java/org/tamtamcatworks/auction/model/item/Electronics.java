package org.tamtamcatworks.auction.model.item;

/**
 * Concrete class for electronic items
 */
public class Electronics extends Item {

    /*
     * Each electronic item will have
     * brand (String)
     * model (String)
     * warrantyMonths (int)
     */

    private String brand;
    private String model;
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