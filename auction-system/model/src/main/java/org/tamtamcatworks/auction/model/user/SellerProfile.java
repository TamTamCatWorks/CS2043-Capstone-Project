package org.tamtamcatworks.auction.model.user;

import org.tamtamcatworks.auction.model.Entity;
import java.util.List;

public class SellerProfile extends Entity {
    private List<String> listings;
    private double rating;

    protected SellerProfile(List<String> listings, double rating) {
        this.listings = listings;
        this.rating = rating;
    }

    public List<String> getListings() { return listings;}
    public double getRating() {return rating;}

    public void setListings(List<String> newListings) {
        this.listings = newListings;
    }

    public void setRating(double newRating) {
        this.rating = newRating;
    }

	@Override
    public String getDisplayInfo() {
        return "SellerId: " + this.getEntityId() + ", listings: " + listings + ", rating: " + rating ;
    }
}