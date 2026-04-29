package org.tamtamcatworks.auction.model;

import java.time.LocalDateTime;
import java.util.UUID;

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
public abstract class Entity {

    // ── Fields ──────────────────────────────────────────────────────────────────

    /**
     * Định danh duy nhất cho entity này.
     *
     * <p>TẠI SAO DÙNG UUID:
     * - Đảm bảo duy nhất toàn cầu, không trùng lặp
     * - Không phụ thuộc vào database (có sinh trước khi lưu DB)
     * - Phù hợp cho distributed system
     *
     * <p>IMMUTABLE (Bất biến):
     * - final → không thể thay đổi sau khi gán
     * - Dùng làm primary key trong database
     * - Nếu cần thay đổi ID → tạo entity mới
     */
    private final String entityId;

    /**
     * Thời điểm entity được tạo.
     *
     * <p>MỤC ĐÍCH:
     * - Theo dõi thời gian tạo để audit log
     * - Sắp xếp theo thời gian (mới nhất trước)
     * - Tính toán tuổi của entity
     *
     * <p>IMMUTABLE (Bất biến):
     * - final → không thể sửa thời gian tạo
     * - Nếu cần ghi thời gian cập nhật → dùng field updatedAt riêng
     */
    private final LocalDateTime createdAt;

    // ── Constructors ─────────────────────────────────────────────────────────────

    /**
     * Constructor tạo entity mới.
     *
     * <p>LOGIC:
     * 1. Sinh UUID ngẫu nhiên làm entityId
     * 2. Lấy thời gian hiện tại làm createdAt
     * 3. Cả hai field đều final → chỉ gán một lần
     *
     * <p>TẠI SAO protected:
     * - Không cho phép tạo Entity trực tiếp (abstract)
     * - Chỉ subclass (Item, User, Auction...) mới gọi được
     * - Subclass sẽ gọi super() trong constructor của mình
     *
     * @implSpec Sử dụng UUID.randomUUID() để sinh ID duy nhất
     */
    protected Entity() {
        this.entityId = UUID.randomUUID().toString();
        this.createdAt = LocalDateTime.now();
    }

    /**
     * Constructor load entity từ database.
     *
     * <p>TẠI SAO CẦN CONSTRUCTOR NÀY:
     * - Khi load từ DB → đã có entityId và createdAt sẵn
     * - Không thể dùng constructor default vì sẽ sinh ID mới (sai)
     * - Cần khôi phục lại chính xác trạng thái lưu trong DB
     *
     * <p>LOGIC:
     * - Gán trực tiếp entityId và createdAt từ tham số
     * - Không sinh mới, không validate (đã validate khi lưu DB)
     *
     * @param entityId UUID đã có từ database
     * @param createdAt timestamp đã có từ database
     */
    protected Entity(String entityId, LocalDateTime createdAt) {
        this.entityId = entityId;
        this.createdAt = createdAt;
    }

    // ── Getters ──────────────────────────────────────────────────────────────────

    /**
     * Lấy định danh duy nhất của entity.
     *
     * @return chuỗi UUID, không bao giờ null (được sinh trong constructor)
     */
    public String getEntityId() {
        return entityId;
    }

    /**
     * Lấy thời điểm entity được tạo.
     *
     * @return LocalDateTime thời điểm tạo, không bao giờ null
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    // ── Abstract method ─────────────────────────────────────────────────────────

    /**
     * Trả về chuỗi mô tả entity để hiển thị cho người dùng.
     *
     * <p>POLYMORPHISM (Đa hình):
     * - Entity khai báo abstract method
     * - Item implement: hiển thị tên, giá, tình trạng...
     * - User implement: hiển thị username, balance...
     * - Auction implement: hiển thị title, trạng thái, giá hiện tại...
     * - Khi gọi entity.getDisplayInfo(), Java tự gọi method đúng theo loại thực tế
     *
     * <p>TẠI SAO CẦN METHOD NÀY:
     * - Cung cấp cách hiển thị thống nhất cho mọi entity
     * - Dùng cho logging, debugging, UI display
     * - Tránh phải if-else kiểm tra loại entity
     *
     * @return chuỗi mô tả ngắn gọn, dễ đọc cho người dùng
     */
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
        if (this == obj) return true;  // Cùng vùng nhớ → chắc chắn bằng nhau
        if (!(obj instanceof Entity other)) return false;  // Không phải Entity → khác nhau
        return entityId != null && entityId.equals(other.entityId);  // So sánh theo ID
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
        return entityId != null ? entityId.hashCode() : 0;
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
        return getClass().getSimpleName() + "{id='" + entityId + "'}";
    }
}