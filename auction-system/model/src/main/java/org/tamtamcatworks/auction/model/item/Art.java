package org.tamtamcatworks.auction.model.item;

/**
 * Concrete class for art items
 */
public class Art extends Item {

    /*
     * Each art item will have
     * artist (String)
     * yearCreated (int)
     * medium (String)
     * hasCertificate (boolean)
     */

    /**
     * Tạo sản phẩm nghệ thuật mới.
     *
     * name: tên sản phẩm
     * description: mô tả
     * startingPrice: giá khởi điểm
     * condition: tình trạng
     * sellerId: id người bán
     * artist: tên tác giả
     * yearCreated: năm sáng tác
     * medium: chất liệu / kỹ thuật
     * hasCertificate: có chứng chỉ xác thực không
     */
    public Art(

    ){
        // Gọi super(...) với ItemType.ART
        // Gán giá trị cho các field
    }

    /**
     * Trả về thông tin chuyên biệt của Art.
     * - Format: "artist | yearCreated | medium | certificate"
     */
    @Override
    public String getDisplayInfo() {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}