
# The Problem & Scope of Implementation
- Bidding system is a platform for multiple users to compete for a product or services in a determined time. Instead of a fixed price, the seller list their product to the system, and the final price is decided by the bidding process by the bidders

# System Design
## Main Classes (0.5p)
```classDiagram
direction LR

class BaseEntity {
  <<mappedSuperclass>>
  +String id
  +LocalDateTime creationDate
}

class User {
  +String username
  +String email
  +String passwordHash
  +String fullName
  +double balance
  +double holdBalance
  +boolean active
}

class BuyerProfile {
  +int totalWins
  +double totalSpent
  +List~String~ biddingHistory
  +List~String~ watchlist
}

class SellerProfile {
  +double rating
  +int ratingCount
  +double totalRevenue
  +int totalSold
  +List~String~ listings
}

class AdminProfile {
  +List~String~ permissions
  +List~String~ actionLog
}

class Auction {
  +String title
  +double startingPrice
  +double currentPrice
  +double minimumIncrement
  +LocalDateTime startTime
  +LocalDateTime endTime
  +AuctionStatus status
  +Long version
}

class Item {
  <<abstract>>
  +String name
  +String description
  +double startingPrice
  +ItemCondition condition
  +String imageUrl
  +LocalDateTime listedAt
}

class Art
class Electronics
class Vehicle
class Other

class BidTransaction {
  +double amount
  +BidType bidType
}

class AutoBid {
  +double maxBid
  +boolean active
}

class Notification {
  +String userId
  +NotificationType type
  +String message
  +boolean read
}

BaseEntity <|-- User
BaseEntity <|-- BuyerProfile
BaseEntity <|-- SellerProfile
BaseEntity <|-- AdminProfile
BaseEntity <|-- Auction
BaseEntity <|-- Item
BaseEntity <|-- BidTransaction
BaseEntity <|-- AutoBid
BaseEntity <|-- Notification

Item <|-- Art
Item <|-- Electronics
Item <|-- Vehicle
Item <|-- Other

User "1" o-- "0..1" BuyerProfile : buyerProfile
User "1" o-- "0..1" SellerProfile : sellerProfile
User "1" o-- "0..1" AdminProfile : adminProfile

User "1" <-- "0..*" Auction : seller
User "1" <-- "0..*" Auction : leadingBidder
Auction "1" --> "1" Item : item
Auction "1" o-- "0..*" BidTransaction : bidHistory

Auction "1" <-- "0..*" BidTransaction : auction
User "1" <-- "0..*" BidTransaction : bidder

Auction "1" <-- "0..*" AutoBid : auction
User "1" <-- "0..*" AutoBid : bidder

Notification ..> User : userId only
```

## OOP (Domain-Rich, Encapsulation, Inheritance, Polymorphism, Abstraction) (1p)

## Design Patterns (Factory, Observer, Builder, Singleton,....) (BIG thank you to spring) (1p)

# The Architecture - Modules, Layers, Tech Stack
## Client-Server Architecture, Tech Stack (0.5p)
## MVC Application (0.5p)
### Client MVC (FXML - JavaFX controllers - Shared Java Request/Response)
### Server MVC (Shared Java Request/Response - Spring Controller - Spring Services - JPA Entity - JPA Repository)

## Integration with Maven (0.5p)

## Unit Test with JUnit (0.5p)

## CI/CD with GitHub Actions (0.5p)

# Specifics Functionalities

## User/Item Management (1p)

### User Management

### Auction Item Management

## Auction Functionalities (1p)
### Bidding
### Auction Closure & Management with Spring Scheduler
### Global Exception Handling with @RestControllerAdvice
### GUI with javaFX & atlantaFX

## Concurrency & Realtime Update
### Concurrent Bidding Handling (Thanks to Spring) (1p)
### Realtime Update (Observer/Websockets) (0.5p)

## Further Functionalities (0.5 per), max (1.5p)
### Anti-sniping
### Auto-bidding
### Bid History Visualization with Line chart
### Search & Pagination
### Object Storage with Minio




