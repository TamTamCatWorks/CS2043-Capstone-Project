# Model Module - Architecture Documentation

## Tổng quan

Hệ thống đấu giá này được thiết kế theo hướng **Domain-Driven Design (DDD)** với các design patterns phổ biến để đảm bảo tính mở rộng, bảo trì và thread-safety.

## Cấu trúc Domain Model

```
auction
├──model/
│   ├── Entity.java                 # Base class cho tất cả entities
│   ├── Auction.java                # Phiên đấu giá (Subject trong Observer)
│   ├── BidTransaction.java         # Bản ghi bid (immutable value object)
│   ├── AuctionStatus.java          # Enum trạng thái phiên
│   ├── role/                       # Roles tạm thời (chỉ tồn RAM)
│   │   ├── AuctionRole.java       # Interface role
│   │   ├── BidderRole.java         # Role bidder (implement AuctionObserver)
│   │   └── SellerRole.java         # Role seller (implement AuctionObserver)
│   ├── user/                       # User và profiles (lưu DB)
│   │   ├── User.java               # Entity user
│   │   ├── BuyerProfile.java       # Hồ sơ mua hàng
│   │   ├── SellerProfile.java      # Hồ sơ bán hàng
│   │   └── AdminProfile.java      # Hồ sơ admin
│   └── item/                       # Items
│       ├── Item.java               # Base class item
│       ├── ItemType.java           # Enum loại item
│       ├── ItemCondition.java      # Enum tình trạng item
│       └── Art.java                # Item loại Art
└── observer/                   # Observer Pattern
    ├── AuctionObserver.java    # Interface observer
    ├── AuctionEvent.java       # Event object (immutable)
    └── AuctionEventType.java  # Enum loại event
```

## Design Patterns Được Sử Dụng

### 1. Observer Pattern

**Thành phần:**
- **Subject:** `Auction` - giữ danh sách observers và notify khi có sự kiện
- **Observer:** `AuctionObserver` interface - định nghĩa contract cho observers
- **Concrete Observers:** `BidderRole`, `SellerRole` - implement interface để nhận event
- **Event:** `AuctionEvent` - immutable object chứa thông tin event

**Tại sao dùng Observer Pattern?**
- **Decoupling:** Auction không cần biết chi tiết về observer, chỉ cần biết họ implement AuctionObserver
- **Real-time notification:** Khi có bid mới, tất cả observers được cập nhật ngay lập tức
- **Scalability:** Dễ thêm observer mới (EmailNotifier, WebSocket, Logging...) mà không sửa Auction

**Ví dụ luồng event:**
```
Bidder đặt giá → BidProcessor.validate() → Auction.recordBid()
→ Auction.notifyObservers(BID_PLACED)
→ BidderRole.onAuctionEvent() → cập nhật holdAmount
→ SellerRole.onAuctionEvent() → cập nhật trạng thái
```

### 2. Builder Pattern

**Thành phần:**
- `AuctionEvent.Builder` - tạo event với nhiều optional parameters
- Fluent API: `.message("...").data("key", value).build()`

**Tại sao dùng Builder?**
- **Readability:** Code dễ đọc hơn constructor với nhiều tham số
- **Optional parameters:** Không cần truyền null cho tham số không dùng
- **Immutability:** Builder có thể build immutable object

### 3. Value Object Pattern

**Thành phần:**
- `BidTransaction` - immutable record của bid
- `AuctionEvent` - immutable event object

**Tại sao dùng Value Object?**
- **Audit trail:** Bid không thể thay đổi sau khi tạo (integrity của lịch sử)
- **Thread-safe:** Immutable objects có thể truyền giữa các thread an toàn
- **No identity:** Hai bid bằng nhau nếu tất cả field bằng nhau

## Quyết Định Thiết Kế Quan Trọng

### 1. Immutable Fields trong Entity

**Thiết kế:**
- `entityId` và `createdAt` trong `Entity` là `final`
- Tất cả fields trong `BidTransaction` và `AuctionEvent` là `final`

**Tại sao?**
- **Consistency:** ID và timestamp không bao giờ thay đổi → dùng làm primary key trong DB
- **Thread-safety:** Immutable fields không cần synchronized khi đọc
- **Audit trail:** Lịch sử bid và event phải giữ nguyên trạng thái

### 2. Hai Constructor cho Entity

**Thiết kế:**
```java
protected Entity()                              // Tạo mới entity từ code
protected Entity(String id, LocalDateTime createdAt)  // Load từ DB
```

**Tại sao?**
- **Code path:** Khi tạo mới → sinh UUID mới, timestamp hiện tại
- **DB path:** Khi load từ DB → dùng ID và timestamp đã có, không sinh mới
- Tránh việc load entity từ DB nhưng lại có ID khác với DB

### 3. Tách biệt Role và Profile

**Thiết kế:**
- **Role (tạm thời):** `BidderRole`, `SellerRole` - chỉ tồn trên RAM, xóa khi leave auction
- **Profile (lâu dài):** `BuyerProfile`, `SellerProfile` - lưu vào DB, tồn tại vĩnh viễn

**Tại sao?**
- **Role:** State runtime của user trong phiên đấu giá (holdAmount, leading status...)
- **Profile:** Lịch sử lâu dài (tổng chi, doanh thu, rating...)
- Tách biệt giúp:
  - Role nhẹ, có thể GC khi không cần
  - Profile lưu DB để phân tích, thống kê

### 4. Synchronized Methods trong Auction

**Thiết kế:**
- Các method thay đổi state (`open`, `recordBid`, `close`, `cancel`, `extendEndTime`) đều `synchronized`

**Tại sao?**
- **Thread-safety:** Tránh race condition khi nhiều bidder đặt giá cùng lúc
- **Atomic state transition:** Đảm bảo status và data luôn nhất quán
- **Observer safety:** observers list cũng được bảo vệ bằng synchronized

**Ví dụ race condition nếu không synchronized:**
```
Thread A: bid 100,000 → currentPrice = 100,000
Thread B: bid 150,000 → currentPrice = 150,000
Thread A: update leadingBidder = A (sai vì B mới là leading)
```

### 5. Validation ở BidProcessor, không ở Auction.recordBid

**Thiết kế:**
- `BidProcessor.validateBid()` - kiểm tra logic business (balance, minimum increment...)
- `Auction.recordBid()` - chỉ ghi nhận bid đã được validate

**Tại sao?**
- **Separation of concerns:** Auction chỉ quản lý state phiên, không quản lý logic bid
- **Single Responsibility:** Mỗi class có 1 trách nhiệm duy nhất
- **Testability:** Dễ test riêng biệt validation và recording

### 6. UnmodifiableList cho bidHistory

**Thiết kế:**
```java
public List<BidTransaction> getBidHistory() {
    return Collections.unmodifiableList(bidHistory);
}
```

**Tại sao?**
- **Append-only:** bidHistory không thể sửa, chỉ thêm mới
- **Integrity:** Tránh client code thêm/xóa bid không qua recordBid()
- **Audit trail:** Đảm bảo lịch sử bid chính xác

## Mối Quan Hệ Giữa Các Class

### Inheritance Hierarchy
```
Entity (abstract)
├── Item (abstract)
│   └── Art
├── User
└── Auction
```

### Observer Pattern Relationships
```
Auction (Subject)
├── registerObserver(AuctionObserver)
├── removeObserver(AuctionObserver)
└── notifyObservers(AuctionEvent)

AuctionObserver (interface)
├── BidderRole (implement)
└── SellerRole (implement)
```

### User - Role - Profile Relationships
```
User
├── auctionRoles: Map<String, AuctionRole>  (tạm thời, RAM)
│   ├── BidderRole
│   └── SellerRole
├── buyerProfile: BuyerProfile  (lâu dài, DB)
└── sellerProfile: SellerProfile (lâu dài, DB)
```

## State Machine của Auction

```
PENDING → ACTIVE → CLOSED (luồng bình thường)
   ↓         ↓
CANCELLED  CANCELLED (có thể hủy bất kỳ lúc nào)
```

- **PENDING:** Phiến được tạo, chưa mở
- **ACTIVE:** Đang nhận bid
- **CLOSED:** Kết thúc bình thường, có người thắng
- **CANCELLED:** Bị hủy, hoàn tiền cho bidder

## Event Lifecycle

```
1. AUCTION_STARTED (khi open())
2. BID_PLACED (khi recordBid()) - có thể lặp nhiều lần
3. AUCTION_EXTENDED (khi extendEndTime()) - có thể lặp nhiều lần
4. AUCTION_CLOSED (khi close())
5. AUCTION_CANCELLED (khi cancel())
6. AUCTION_WON (khi close và có người thắng)
```

## Thread-Safety Strategy

1. **Auction:** synchronized methods cho state-changing operations
2. **Immutable objects:** BidTransaction, AuctionEvent không cần synchronized
3. **Unmodifiable collections:** bidHistory, data trong AuctionEvent

## Tài Liệu Tham Khảo

- Domain-Driven Design (Eric Evans)
- Design Patterns: Elements of Reusable Object-Oriented Software (Gang of Four)
- Java Concurrency in Practice (Brian Goetz)
