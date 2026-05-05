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
    private String artist;
    private int yearCreated;
    private String medium;
    private String dimensions;
    private boolean hasCertificate;

    /**
     * Tạo sản phẩm nghệ thuật mới.
     * <p>
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
            String name,
            String description,
            double startingPrice,
            ItemCondition condition,
            String sellerId,
            String artist,
            int yearCreated,
            String medium,
            boolean hasCertificate) {
        super(name, description, startingPrice, ItemType.ART, condition, sellerId);
        this.artist = artist;
        this.yearCreated = yearCreated;
        this.medium = medium;
        this.hasCertificate = hasCertificate;
    }

    /**
     * Trả về thông tin chuyên biệt của Art.
     * - Format: "artist | yearCreated | medium | certificate"
     */

    @Override
    public String getSpecificInfo() {
        String cert = hasCertificate ? "Có chứng chỉ xác thực" : "Không có chứng chỉ";
        return "Tác giả: " + artist
                + " | Năm: " + yearCreated
                + " | Chất liệu: " + medium
                + " | " + cert;
    }
    // ── Getters & Setters ────────────────────────────────────────────────────────

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public int getYearCreated() {
        return yearCreated;
    }

    public void setYearCreated(int yearCreated) {
        this.yearCreated = yearCreated;
    }

    public String getMedium() {
        return medium;
    }

    public void setMedium(String medium) {
        this.medium = medium;
    }

    public String getDimensions() {
        return dimensions;
    }

    public void setDimensions(String dimensions) {
        this.dimensions = dimensions;
    }

    public boolean isHasCertificate() {
        return hasCertificate;
    }

    public void setHasCertificate(boolean hasCertificate) {
        this.hasCertificate = hasCertificate;
    }
}