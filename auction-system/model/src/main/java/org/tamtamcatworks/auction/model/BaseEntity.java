package org.tamtamcatworks.auction.model;

import java.time.LocalDateTime;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;

/**
 * Lớp trừu tượng cơ sở cho tất cả các entity trong domain model.
 *
 * <p>INHERITANCE (Sự kế thừa):
 * - Entity là lớp gốc trong hệ thống phân cấp model
 * - Item, User, Auction... đều kế thừa từ Entity
 * - Cung cấp các field chung: entityId (UUID) và createdAt (timestamp)
 *
 * <p>ABSTRACTION (Tính trừu tượng):
 * - Entity là abstract class → không thể tạo trực tiếp
 * - Bắt buộc subclass phải implement getDisplayInfo()
 * - Mỗi loại entity có cách hiển thị thông tin riêng (polymorphism)
 *
 * <p>IMMUTABLE FIELDS (Các field bất biến):
 * - entityId: UUID sinh tự động, không thay đổi sau khi tạo
 * - createdAt: timestamp ghi thời điểm tạo, không sửa được
 * - Tại sao bất biến? Đảm bảo tính nhất quán, dùng làm primary key trong DB
 *
 * <p>WHY TWO CONSTRUCTORS (Tại sao có 2 constructor):
 * 1. Entity() - tạo mới entity từ code (sinh UUID mới, timestamp hiện tại)
 * 2. Entity(id, createdAt) - load từ DB (dùng ID và timestamp đã có)
 *
 * @author R&D (Nguyen Hoang Vu)
 * @version 1.0
 */

@MappedSuperclass
public abstract class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private LocalDateTime creationDate;


    @PrePersist
    protected void onCreate() {
        this.creationDate = LocalDateTime.now();
    }

    public String getEntityId() {
        return id;
    }


    public LocalDateTime getCreationDate() {
        return creationDate;
    }


    public abstract String getDisplayInfo();

    // ── Override Object methods ─────────────────────────────────────────────────

    /**
     * So sánh hai entity có bằng nhau không.
     *
     * <p>LOGIC SO SÁNH:
     * - Hai entity bằng nhau khi có cùng entityId
     * - KHÔNG so sánh vùng nhớ (reference)
     * - KHÔNG so sánh các field khác (createdAt, content...)
     *
     * <p>TẠI SAO DÙNG ENTITYID:
     * - entityId là unique identifier → đủ để xác định entity
     * - So sánh theo ID phù hợp với database (primary key)
     * - Cho phép entity được load từ DB khác nhau nhưng cùng ID vẫn bằng nhau
     *
     * <p>CONTRACT (Quy tắc):
     * - Reflexive: x.equals(x) = true
     * - Symmetric: x.equals(y) = y.equals(x)
     * - Transitive: x.equals(y) && y.equals(z) → x.equals(z)
     * - Consistent: gọi nhiều lần trả về kết quả giống nhau
     * - null-safe: x.equals(null) = false
     *
     * @param obj object cần so sánh
     * @return true nếu cùng entityId, false nếu khác hoặc null
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof BaseEntity other)) return false;
        return id != null && id.equals(other.id);
    }

    /**
     * Tính hash code dựa trên entityId.
     *
     * <p>TẠI SAO CẦN OVERRIDE:
     * - Contract: nếu equals() trả về true → hashCode() phải trả về cùng giá trị
     * - Mặc định hashCode() dùng vùng nhớ → sẽ khác với equals() dựa trên ID
     * - Cần đồng bộ để dùng entity trong HashMap, HashSet...
     *
     * <p>LOGIC:
     * - hashCode = entityId.hashCode()
     * - Nếu entityId null → trả về 0
     *
     * @return hash code dựa trên entityId
     */
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
    /**
     * Chuỗi đại diện cho entity (dùng cho debugging).
     *
     * <p>FORMAT:
     * - ClassName{id='entityId'}
     * - Ví dụ: Item{id='123e4567-e89b-12d3-a456-426614174000'}
     *
     * <p>TẠI SAO FORMAT NÀY:
     * - Ngắn gọn, dễ đọc
     * - Chứa đủ thông tin quan trọng (loại + ID)
     * - Không chứa sensitive data
     *
     * @return chuỗi mô tả entity
     */
    @Override
    public String toString() {
        return getClass().getSimpleName() + "{id='" + id + "'}";
    }
}