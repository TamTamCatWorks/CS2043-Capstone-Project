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

    ){
        // Gọi super(...) với ItemType.ELECTRONICS
        // Gán giá trị cho các field
    }

    /**
     * Trả về thông tin chuyên biệt của Electronics.
     * - Format: "brand | model | warranty"
     */
    @Override
    public String getDisplayInfo() {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}