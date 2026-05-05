package org.tamtamcatworks.auction.model.item;

/**
 * Phương tiện: ô tô, xe máy, thuyền, máy bay cá nhân...
 */
public class Vehicle extends Item {

    /*
     * Each vehicle will have:
     * make (String)
     * model (String)
     * year (int)
     * mileageKm (int)
     * color (String)
     * fuelType (String)
     */

    private String make;
    private String model;
    private int year;
    private int mileageKm;
    private String color;
    private String fuelType;

    /**
     * Tạo phương tiện mới.
     *
     * name: tên hiển thị (vd: "Toyota Camry 2022")
     * description: mô tả
     * startingPrice: giá khởi điểm
     * condition: tình trạng
     * sellerId: id người bán
     * make: hãng xe
     * model: dòng xe
     * year: năm sản xuất
     * mileageKm: số km đã đi
     * color: màu xe
     * fuelType: loại nhiên liệu
     */
    public Vehicle(
            String name,
            String description,
            double startingPrice,
            ItemCondition condition,
            String sellerId,
            String make,
            String model,
            int year,
            int mileageKm,
            String color,
            String fuelType) {
        super(name, description, startingPrice, ItemType.VEHICLE, condition, sellerId);
        if (mileageKm < 0) {
            throw new IllegalArgumentException("Số km đã đi không được âm.");
        }
        this.make = make;
        this.model = model;
        this.year = year;
        this.mileageKm = mileageKm;
        this.color = color;
        this.fuelType = fuelType;
    }

    /**
     * Trả về thông tin chuyên biệt của Vehicle.
     * - Format:
     *   "make model year | km | color | fuelType"
     */
    @Override
    public String getSpecificInfo() {
        return make + " " + model + " " + year
                + " | " + mileageKm + " km"
                + " | Màu: " + color
                + " | Nhiên liệu: " + fuelType;
    }
}