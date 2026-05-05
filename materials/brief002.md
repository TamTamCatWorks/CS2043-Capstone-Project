# MongoDB vs PostgreSQL
## MongoDB
- MongoDB is a document database
- Basically in everything in one document, the data can be whatever you want
- Very easy for data that's loosely related, like our auction item.

### Main Concern 
- Not a relational database, so it's less standard
- Transaction need to be handled carefully to prevent race conditions
- Practically zero thought in database design, so no bonus credit in that

## PostgreSQL
- A little harder to use, but with JPA it's not that bad. 
- You have to annotate your fields
- Bit harder to use on the cloud, but that's not the concern right now

# Cheatsheet
## Key Vocabularies

| Term | Meaning |
|---|---|
| `@Entity` | Marks a class as a database table |
| `@Id` | Primary key field |
| `@GeneratedValue` | Auto-generate the ID (UUID, sequence, etc.) |
| `@Column` | Maps a field to a column, optional if name matches |
| `@Enumerated` | Stores an enum as STRING or ORDINAL |
| `@OneToMany` / `@ManyToOne` | Relationship between tables |
| `@ManyToMany` | Many-to-many relationship, needs a join table |
| `@JoinColumn` | Specifies the foreign key column |
| `@Transient` | Field is NOT persisted to the database |
| `@Version` | Optimistic locking — prevents lost updates |
| `JpaRepository` | Interface that gives you CRUD + queries for free |
| `@Transactional` | Wraps a method in a DB transaction, rolls back on failure |
| `ddl-auto` | Controls whether Hibernate creates/updates/drops tables |

## application.yml

```yaml
# main/resources — PostgreSQL
spring:
  datasource:
    url: ${DB_URL:jdbc:postgresql://localhost:5432/tamtamcatworks}
    username: ${DB_USER:postgres}
    password: ${DB_PASS:postgres}
  jpa:
    hibernate:
      ddl-auto: update   # create-drop for dev, validate for prod
    show-sql: true

# test/resources — H2 (overrides above for tests)
spring:
  datasource:
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver
  jpa:
    hibernate:
      ddl-auto: create-drop
```

## `ddl-auto` Values

| Value         | What it does                                 | Use when                       |
| ------------- | -------------------------------------------- | ------------------------------ |
| `create-drop` | Creates on start, drops on stop              | Tests                          |
| `create`      | Creates tables, never drops                  | Early dev                      |
| `update`      | Adds missing columns, never drops            | Active dev                     |
| `validate`    | Checks schema matches entities, fails if not | Production                     |
| `none`        | Does nothing                                 | Production (manual migrations) |

# Work to be done 
## Adding JPA annotations to the model & Modify some logic
```java
@MappedSuperclass
public abstract class Entity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private LocalDateTime creationDate;
}
```

```java
// base table: item (id, name, starting_price, item_type, item_condition)
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "item_type")
public abstract class Item extends Entity {


    private String name;
    private Double startingPrice;

    @Enumerated(EnumType.STRING)
    private ItemCondition itemCondition;

    @Enumerated(EnumType.STRING)
    private ItemType itemType;
}

// art table: (id FK → item.id, artist, medium)
@Entity
@DiscriminatorValue("ART")
public class Art extends Item {
    private String artist;
    private String medium;
}

// electronics table: (id FK → item.id, brand, model)
@Entity
@DiscriminatorValue("ELECTRONICS")
public class Electronics extends Item {
    private String brand;
    private String model;
}

// vehicle table: (id FK → item.id, make, model, year)
@Entity
@DiscriminatorValue("VEHICLE")
public class Vehicle extends Item {
    private String make;
    private String model;
    private int year;
}
```

```java
@Entity
public class Auction extends Entity {

    // one auction has many bids
    @OneToMany(mappedBy = "auction", cascade = CascadeType.ALL)
    private List<BidTransaction> bids = new ArrayList<>();

    // one auction has one item
    @OneToOne
    @JoinColumn(name = "item_id")
    private Item item;

    @Enumerated(EnumType.STRING)
    private AuctionStatus status;

    @Version
    private Long version; // prevents lost updates on concurrent bids
}

@Entity
public class BidTransaction {

    @Id
    private String id;

    @ManyToOne
    @JoinColumn(name = "auction_id")
    private Auction auction;

    private Double amount;
}
```

## Adding Repositories to Persist them

```java
public interface ItemRepository extends JpaRepository<Item, String> {
    List<Item> findByItemType(ItemType itemType);
    List<Item> findByItemCondition(ItemCondition condition);
    List<Item> findByStartingPriceBetween(Double min, Double max);
}
```

Querying returns the concrete subtype at runtime:

```java
List<Item> items = itemRepository.findByItemType(ItemType.ART);
Art art = (Art) items.get(0); // safe, Hibernate resolved it
```


## Implementing some basic services

```java
@Service
public class AuctionService {

    @Transactional(rollbackFor = Exception.class)
    public void placeBid(String auctionId, BidTransaction bid) {
        Auction auction = auctionRepo.findById(auctionId)
            .orElseThrow(() -> new RuntimeException("Auction not found"));

        auction.getBids().add(bid);
        auctionRepo.save(auction); // @Version field catches concurrent updates
                                   // throws OptimisticLockingFailureException on conflict
    }
}
```


