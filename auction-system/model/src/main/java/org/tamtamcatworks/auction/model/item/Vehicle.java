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

    ){
        // Gọi super(...) với ItemType.VEHICLE
        // Gán giá trị cho các field
        // Validate cơ bản (vd: mileageKm >= 0)
    }

    /**
     * Trả về thông tin chuyên biệt của Vehicle.
     * - Format:
     *   "make model year | km | color | fuelType"
     */
    @Override
    public String getDisplayInfo() {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}