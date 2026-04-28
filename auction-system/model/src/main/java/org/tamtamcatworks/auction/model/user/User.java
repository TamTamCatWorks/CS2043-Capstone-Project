package org.tamtamcatworks.auction.model.user;

import org.tamtamcatworks.auction.model.Entity;

public class User extends Entity {

    private String name;
    private String email;
    private String password;

    private BuyerProfile buyerProfile;
    private SellerProfile sellerProfile;
    private AdminProfile adminProfile;

    protected User (String name, String email, String password) {
        super();
        this.name = name;
        this.email = email;
        this.password = password;
    }

    public String getName() { return name; }

    public String getEmail() { return email; }

    public String getPassword() { return password; }

    public BuyerProfile getBuyerProfile() { return buyerProfile; }

    public SellerProfile getSellerProfile() { return sellerProfile; }

    public AdminProfile getAdminProfile() { return adminProfile; }

    public void setName(String newName) { 
		this.name = newName; 
	}

    public void setEmail(String newEmail) { 
		this.email = newEmail; 
	}

    public void setPassword(String newPassword) { 
		this.password = newPassword; 
	}

    public void setBuyerProfile(BuyerProfile newBuyerProfile) {
        this.buyerProfile = newBuyerProfile;
    }

    public void setSellerProfile(SellerProfile newSellerProfile) {
        this.sellerProfile = newSellerProfile;
    }

    public void setAdminProfile(AdminProfile newAdminProfile) {
        this.adminProfile = newAdminProfile;
    }

    @Override
    public String getDisplayInfo() {
       return ("UserId: " + this.getEntityId() + ", name: " + name + ", email: " + email);
    }
}