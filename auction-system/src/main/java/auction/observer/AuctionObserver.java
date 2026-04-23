package auction.observer;

/**
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │  📌 CONCEPT: Observer Pattern                                           │
 * │                                                                         │
 * │  AuctionObserver is the "listener" contract in the Observer pattern:    │
 * │    Subject   = AuctionManager  (calls notifyObservers internally)       │
 * │    Observer  = AuctionObserver (implemented by Bidder, and optionally   │
 * │                                  by a JavaFX controller)               │
 * │    Event     = AuctionEvent    (carries type, auction ref, payload)     │
 * │                                                                         │
 * │  STUDENT QUESTIONS:                                                     │
 * │                                                                         │
 * │  Q1. The spec says "implementors must not throw checked exceptions".    │
 * │      Why is this constraint necessary when AuctionManager calls         │
 * │      onAuctionEvent() inside a loop over all observers? What would      │
 * │      happen if one observer threw a checked exception mid-loop?         │
 * │                                                                         │
 * │  Q2. Should AuctionManager catch RuntimeExceptions thrown by            │
 * │      observers, or let them propagate? What are the tradeoffs of each   │
 * │      approach in a production system?                                   │
 * │                                                                         │
 * │  Q3. AuctionEvent has an Object payload field. This loses type safety.  │
 * │      How could you redesign AuctionEvent using generics                 │
 * │      (e.g. AuctionEvent<T>) so the payload type is known at compile     │
 * │      time? What makes this tricky with a heterogeneous event list?      │
 * │                                                                         │
 * │  📌 JAVAFX POINTER                                                      │
 * │  A JavaFX controller class can implement AuctionObserver directly.     │
 * │  Register it with AuctionManager and call Platform.runLater() inside   │
 * │  onAuctionEvent() to update Labels, TableViews, etc. on the FX thread. │
 * │                                                                         │
 * │  📌 SPRING POINTER                                                      │
 * │  In a Spring application, AuctionObserver maps naturally to            │
 * │  ApplicationListener<ApplicationEvent>. You could replace this custom   │
 * │  interface with Spring's event system and use                           │
 * │  ApplicationEventPublisher.publishEvent() in AuctionManager.           │
 * └─────────────────────────────────────────────────────────────────────────┘
 */
public interface AuctionObserver {

    /**
     * Called by AuctionManager whenever a relevant event occurs.
     *
     * Implementations MUST NOT throw checked exceptions.
     * AuctionEvent carries:
     *   - type    : AuctionEventType (AUCTION_STARTED, BID_PLACED, AUCTION_CLOSED, AUCTION_CANCELLED)
     *   - auction : the Auction this event relates to
     *   - payload : the new BidTransaction for BID_PLACED events, null otherwise
     *
     * @param event the event object describing what happened.
     */
    void onAuctionEvent(AuctionEvent event);
}
