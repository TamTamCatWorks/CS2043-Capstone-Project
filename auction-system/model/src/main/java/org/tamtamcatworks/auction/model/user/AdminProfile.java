package org.tamtamcatworks.auction.model.user;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.tamtamcatworks.auction.model.BaseEntity;

import jakarta.persistence.*;

/**
 * Hồ sơ Admin — lưu DB vĩnh viễn, liên kết 1-1 với User qua userId.
 *
 * KHÔNG kế thừa Entity — dùng userId làm PK/FK.
 *
 * Lưu danh sách quyền hạn (permissions) và log hành động quản trị.
 * Admin KHÔNG tham gia đấu giá — không có BidderRole hay SellerRole.
 *
 * Permission lưu dạng List<String> thay vì EnumSet để dễ serialize
 * xuống DB và linh hoạt thêm permission mới không cần đổi schema.
 * Các giá trị hợp lệ: MANAGE_USERS, MANAGE_ITEMS,
 *                     MANAGE_AUCTIONS, VIEW_LOGS, MANAGE_ADMINS
 */

@Entity
@Table(name = "admin_profiles")
public class AdminProfile extends BaseEntity {

    @OneToOne(mappedBy = "adminProfile")
    private User user;


    @ElementCollection
    @CollectionTable(name = "admin_permissions", joinColumns = @JoinColumn(name = "admin_id"))
    private List<String> permissions = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "admin_action_log", joinColumns = @JoinColumn(name = "admin_id"))
    private List<String> actionLog = new ArrayList<>();

    // ── Constructor ───────────────────────────────────────────────────────────
    protected AdminProfile() {}

    public AdminProfile(List<String> permissions) {
        this.permissions = new ArrayList<>(permissions);
    }

    // Super admin — có toàn bộ quyền
    public static AdminProfile superAdmin() {
        return new AdminProfile(List.of(
            "MANAGE_USERS",
            "MANAGE_ITEMS",
            "MANAGE_AUCTIONS",
            "VIEW_LOGS",
            "MANAGE_ADMINS"
        ));
    }

    // ── Business methods ──────────────────────────────────────────────────────

    public boolean hasPermission(String permission) {
        return permissions.contains(permission);
    }

    public void grantPermission(String permission) {
        if (!permissions.contains(permission)) {
            permissions.add(permission);
            logAction("Cấp quyền: " + permission);
        }
    }

    public void revokePermission(String permission) {
        permissions.remove(permission);
        logAction("Thu hồi quyền: " + permission);
    }

    // Ghi log hành động quản trị — append-only, không xóa được
    public void logAction(String action) {
        String entry = "[" + java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                + "] " + action;
        actionLog.add(entry);
    }

    @Override
    public String toString() {
        return ""
                + "', permissions=" + permissions + "}";
    }

    @Override
    public String getDisplayInfo() {
        return toString();
    }

    public List<String> getPermissions() {
        return Collections.unmodifiableList(permissions);
    }

    public List<String> getActionLog() {
        return Collections.unmodifiableList(actionLog);
    }

    public void setPermissions(List<String> permissions) {
        this.permissions.clear();
        this.permissions.addAll(permissions);
    }
}