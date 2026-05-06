package org.tamtamcatworks.auction.model.item;

import org.tamtamcatworks.auction.model.user.User;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;

@Entity
@Table(name = "art_items")
@DiscriminatorValue("ART")
@PrimaryKeyJoinColumn(name = "item_id")
public class Art extends Item {

     @Column(nullable = false)
    private String artist;

     @Column(nullable = false)
    private int yearCreated;

     @Column(nullable = false)
    private String medium;

     @Column(nullable = false)
    private String dimensions;

     @Column(nullable = false)
    private boolean hasCertificate;

    public Art(
            String name,
            String description,
            double startingPrice,
            ItemCondition condition,
            User seller,
            String artist,
            int yearCreated,
            String medium,
            boolean hasCertificate) {
        super(name, description, startingPrice, condition, seller);
        this.artist = artist;
        this.yearCreated = yearCreated;
        this.medium = medium;
        this.hasCertificate = hasCertificate;
    }
    
    protected Art() {}

    @Override
    public String getSpecificInfo() {
        String cert = hasCertificate ? "Có chứng chỉ xác thực" : "Không có chứng chỉ";
        return "Tác giả: " + artist
                + " | Năm: " + yearCreated
                + " | Chất liệu: " + medium
                + " | " + cert;
    }

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