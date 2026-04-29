package org.tamtamcatworks.auction.factory;

import org.tamtamcatworks.auction.model.item.Art;
import org.tamtamcatworks.auction.model.item.Electronics;
import org.tamtamcatworks.auction.model.item.Item;
import org.tamtamcatworks.auction.model.item.ItemCondition;
import org.tamtamcatworks.auction.model.item.ItemType;
import org.tamtamcatworks.auction.model.item.Vehicle;

/**
 * Factory Pattern — tạo Item subclass đúng loại mà không lộ constructor.
 *
 * <p>FACTORY METHOD PATTERN:
 * Thay vì người gọi phải biết class cụ thể (Electronics, Art, Vehicle),
 * họ chỉ cần truyền ItemType và các tham số — Factory lo phần còn lại.
 *
 * <p>Lợi ích:
 * - Tập trung logic tạo object vào một chỗ
 * - Dễ thêm loại Item mới (thêm case JEWELRY chẳng hạn) mà không sửa code khác
 * - Controller không phụ thuộc vào Electronics/Art/Vehicle cụ thể
 *
 * <p>Ví dụ sử dụng:
 * <pre>
 *   // Tạo điện tử
 *   Item laptop = ItemFactory.create(ItemType.ELECTRONICS, params);
 *
 *   // Tạo nghệ thuật
 *   Item painting = ItemFactory.create(ItemType.ART, params);
 * </pre>
 */
public class ItemFactory {

    // Constructor private — không cho tạo instance (chỉ dùng static methods)
    private ItemFactory() {}

    /**
     * Tạo sản phẩm Electronics.
     *
     * @param name           tên sản phẩm
     * @param description    mô tả
     * @param startingPrice  giá khởi điểm
     * @param condition      tình trạng
     * @param sellerId       id người bán
     * @param brand          hãng sản xuất
     * @param model          tên model
     * @param warrantyMonths số tháng bảo hành
     * @return Electronics mới
     */
    public static Electronics createElectronics(
            String name,
            String description,
            double startingPrice,
            ItemCondition condition,
            String sellerId,
            String brand,
            String model,
            int warrantyMonths) {
        return new Electronics(
                name, description, startingPrice,
                condition, sellerId,
                brand, model, warrantyMonths);
    }

    /**
     * Tạo sản phẩm Art.
     *
     * @param name           tên tác phẩm
     * @param description    mô tả
     * @param startingPrice  giá khởi điểm
     * @param condition      tình trạng
     * @param sellerId       id người bán
     * @param artist         tên tác giả
     * @param yearCreated    năm sáng tác
     * @param medium         chất liệu
     * @param hasCertificate có chứng chỉ không
     * @return Art mới
     */
    public static Art createArt(
            String name,
            String description,
            double startingPrice,
            ItemCondition condition,
            String sellerId,
            String artist,
            int yearCreated,
            String medium,
            boolean hasCertificate) {
        return new Art(
                name, description, startingPrice,
                condition, sellerId,
                artist, yearCreated, medium, hasCertificate);
    }

    /**
     * Tạo sản phẩm Vehicle.
     *
     * @param name          tên xe (vd: "Toyota Camry 2022")
     * @param description   mô tả thêm
     * @param startingPrice giá khởi điểm
     * @param condition     tình trạng
     * @param sellerId      id người bán
     * @param make          hãng xe
     * @param model         dòng xe
     * @param year          năm sản xuất
     * @param mileageKm     số km đã đi
     * @param color         màu xe
     * @param fuelType      loại nhiên liệu
     * @return Vehicle mới
     */
    public static Vehicle createVehicle(
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
        return new Vehicle(
                name, description, startingPrice,
                condition, sellerId,
                make, model, year, mileageKm, color, fuelType);
    }

    /**
     * Overload generic — tạo Item từ ItemType với tham số mặc định.
     * Dùng khi chỉ biết loại item, chưa có đủ thông tin chi tiết.
     * (Thường dùng khi load từ DB hoặc tạo item stub để test.)
     *
     * @param type          loại item
     * @param name          tên sản phẩm
     * @param description   mô tả
     * @param startingPrice giá khởi điểm
     * @param condition     tình trạng
     * @param sellerId      id người bán
     * @return Item với thông tin mặc định theo loại
     * @throws IllegalArgumentException nếu type không hợp lệ
     */
    public static Item createDefault(
            ItemType type,
            String name,
            String description,
            double startingPrice,
            ItemCondition condition,
            String sellerId) {
        return switch (type) {
            case ELECTRONICS -> createElectronics(
                    name, description, startingPrice, condition, sellerId,
                    "Unknown", "Unknown", 0);
            case ART -> createArt(
                    name, description, startingPrice, condition, sellerId,
                    "Unknown", 2024, "Unknown", false);
            case VEHICLE -> createVehicle(
                    name, description, startingPrice, condition, sellerId,
                    "Unknown", "Unknown", 2024, 0, "Trắng", "Xăng");
        };
    }
}
