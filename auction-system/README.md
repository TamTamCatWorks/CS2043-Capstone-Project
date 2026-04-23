# Auction System — Project Skeleton

## Project Structure

```
auction-system/
├── pom.xml                                  Maven build file
└── src/
    ├── main/java/auction/
    │   ├── model/
    │   │   ├── Entity.java                  Abstract — root of domain hierarchy
    │   │   ├── Auction.java                 Concrete — an individual auction
    │   │   ├── AuctionStatus.java           Enum — PENDING/ACTIVE/CLOSED/CANCELLED
    │   │   ├── BidTransaction.java          Concrete — immutable bid record
    │   │   ├── item/
    │   │   │   ├── Item.java                Abstract — base for all auction items
    │   │   │   ├── ItemCondition.java       Enum — NEW/LIKE_NEW/GOOD/FAIR/POOR
    │   │   │   ├── ItemType.java            Enum — ELECTRONICS/ART/VEHICLE
    │   │   │   ├── Electronics.java         Concrete item subclass
    │   │   │   ├── Art.java                 Concrete item subclass
    │   │   │   └── Vehicle.java             Concrete item subclass
    │   │   └── user/
    │   │       ├── User.java                Abstract — base for all users
    │   │       ├── Bidder.java              Concrete — implements AuctionObserver
    │   │       ├── Seller.java              Concrete — owns active listings
    │   │       └── Admin.java               Concrete — RBAC permission system
    │   ├── factory/
    │   │   └── ItemFactory.java             Factory Pattern — creates Item subclasses
    │   ├── manager/
    │   │   └── AuctionManager.java          Singleton Pattern — central controller
    │   ├── observer/
    │   │   ├── AuctionObserver.java         Interface — Observer contract
    │   │   ├── AuctionEvent.java            Value class — event payload
    │   │   └── AuctionEventType.java        Enum — event kinds
    │   ├── exception/
    │   │   ├── AuctionNotFoundException.java
    │   │   ├── AuctionClosedException.java
    │   │   ├── InvalidBidException.java
    │   │   ├── InsufficientFundsException.java
    │   │   ├── ItemNotFoundException.java
    │   │   ├── DuplicateEntityException.java
    │   │   └── UnauthorizedActionException.java
    │   └── ui/
    │       ├── MainApp.java                 JavaFX Application entry point
    │       └── AuctionListController.java   JavaFX FXML controller (sample)
    └── test/java/auction/
        └── AuctionSystemTest.java           JUnit 5 — all 5 autograder scenarios
```

## Getting Started

### Prerequisites
- JDK 17 or later
- Maven 3.8+

### Build & Test
```bash
# Compile everything
mvn compile

# Run the autograder test suite
mvn test

# Run the JavaFX application (after implementing it)
mvn javafx:run
```

## Design Patterns Covered

| Pattern   | Where used                          |
|-----------|-------------------------------------|
| Singleton | `AuctionManager`                    |
| Factory   | `ItemFactory.createItem()`          |
| Observer  | `AuctionObserver` / `AuctionManager`|

## Your Task

Every method body currently throws `UnsupportedOperationException("Not yet implemented")`.  
Replace each TODO with a correct implementation so that `mvn test` passes all scenarios.

Read the **📌 STUDENT QUESTIONS** in each file before coding — they guide your thinking
and will be discussed in the lab session.
