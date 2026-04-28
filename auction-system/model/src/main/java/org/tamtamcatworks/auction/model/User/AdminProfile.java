package org.tamtamcatworks.auction.model.User;

import org.tamtamcatworks.auction.model.Entity;
import java.util.List;

public class AdminProfile extends Entity {
    private List<String> permission;

    protected AdminProfile(List<String> permission) {
        this.permission = permission;
    }

    public String getPermission() {return permission;}

    public void setPermission(List<String> newPermission) {
        this.permission = newPermission;
    }

    public String getDisplayInfo() {
        return "AdminId: " + this.getEntityId() + ", permission: " + permission;
    }
}
