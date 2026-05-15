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
        String imageUrl,
        User seller,
        String artist,
        int yearCreated,
        String medium,
        String dimensions,
        boolean hasCertificate
) {

    super(
        name,
        description,
        startingPrice,
        condition,
        imageUrl,
        seller
    );

    setArtist(artist);
    setYearCreated(yearCreated);
    setMedium(medium);
    setDimensions(dimensions);
    setHasCertificate(hasCertificate);
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

        if (artist == null || artist.isBlank()) {
            throw new IllegalArgumentException("Artist is required.");
        }

        this.artist = artist.trim();
    }

    public int getYearCreated() {
        return yearCreated;
    }

    public void setYearCreated(int yearCreated) {

        if (yearCreated <= 0) {
            throw new IllegalArgumentException("Invalid year created.");
        }

        this.yearCreated = yearCreated;
    }

    public String getMedium() {
        return medium;
    }

    public void setMedium(String medium) {

        if (medium == null || medium.isBlank()) {
            throw new IllegalArgumentException("Medium is required.");
        }

        this.medium = medium.trim();
    }

    public String getDimensions() {
        return dimensions;
    }

    public void setDimensions(String dimensions) {

        if (dimensions == null || dimensions.isBlank()) {
            throw new IllegalArgumentException("Dimensions are required.");
        }

        this.dimensions = dimensions.trim();
    }

    public boolean isHasCertificate() {
        return hasCertificate;
    }

    public void setHasCertificate(boolean hasCertificate) {
        this.hasCertificate = hasCertificate;
    }
}