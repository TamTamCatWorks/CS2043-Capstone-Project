package org.tamtamcatworks.auction.model.user;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
public class AdminProfile {

    private final String userId;

    // Danh sách quyền hạn — lưu dạng String để dễ serialize DB
    private final List<String> permissions;

    // Log các hành động quản trị — append-only
    private final List<String> actionLog;

    // ── Constructor ───────────────────────────────────────────────────────────

    public AdminProfile(String userId, List<String> permissions) {
        this.userId = userId;
        this.permissions = new ArrayList<>(permissions);
        this.actionLog = new ArrayList<>();
    }

    // Super admin — có toàn bộ quyền
    public static AdminProfile superAdmin(String userId) {
        return new AdminProfile(userId, List.of(
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
        return "AdminProfile{userId='" + userId
                + "', permissions=" + permissions + "}";
    }

    // ── Getters / Setters ─────────────────────────────────────────────────────

    public String getUserId() {
        return userId;
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