package auction.ui;

import auction.manager.AuctionManager;
import auction.model.Auction;
import auction.observer.AuctionEvent;
import auction.observer.AuctionObserver;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;

/**
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │  📌 CONCEPT: MVC in JavaFX — Controller Layer                          │
 * │                                                                         │
 * │  FXMLLoader instantiates this controller and injects @FXML fields.     │
 * │  The controller mediates between the model (AuctionManager) and the    │
 * │  view (FXML file). It also implements AuctionObserver so it can        │
 * │  react to live auction events and refresh the UI.                      │
 * │                                                                         │
 * │  STUDENT QUESTIONS:                                                     │
 * │                                                                         │
 * │  Q1. @FXML fields are null until FXMLLoader calls initialize().         │
 * │      What happens if you try to use auctionTable before initialize()?  │
 * │                                                                         │
 * │  Q2. ObservableList is used instead of a plain List so that the         │
 * │      TableView can automatically detect add/remove and refresh itself.  │
 * │      What other JavaFX collection wrappers exist, and when would you    │
 * │      use ObservableMap?                                                 │
 * │                                                                         │
 * │  Q3. onAuctionEvent() is called from whatever thread called             │
 * │      AuctionManager methods. Why is Platform.runLater() critical here,  │
 * │      and what is the risk of removing it?                               │
 * │                                                                         │
 * │  📌 SPRING POINTER                                                      │
 * │  With SpringFX/fx-weaver, this class becomes a @Component. Spring      │
 * │  can then @Autowired inject AuctionManager (a @Service bean) here      │
 * │  instead of calling getInstance(). This enables proper dependency       │
 * │  injection and makes the controller unit-testable with @MockBean.       │
 * └─────────────────────────────────────────────────────────────────────────┘
 */
public class AuctionListController implements AuctionObserver {

    @FXML
    private TableView<Auction> auctionTable;  // TODO: add TableColumn fields

    @FXML
    private Label statusLabel;

    private final AuctionManager manager = AuctionManager.getInstance();
    private final ObservableList<Auction> auctions = FXCollections.observableArrayList();

    /**
     * Called automatically by FXMLLoader after all @FXML fields are injected.
     */
    @FXML
    public void initialize() {
        // TODO: configure TableView columns
        // TODO: auctionTable.setItems(auctions)
        // TODO: manager.registerObserver(this)
        // TODO: loadAuctions()
    }

    /** Reload all active auctions from AuctionManager into the ObservableList. */
    private void loadAuctions() {
        auctions.setAll(manager.getActiveAuctions());
    }

    /** Called when the "Create Auction" button is clicked. */
    @FXML
    private void onCreateAuction() {
        // TODO: open a dialog / new Scene to collect item + seller info
        // TODO: call manager.createAuction(...)
    }

    /** Called when the "Start Auction" button is clicked. */
    @FXML
    private void onStartAuction() {
        Auction selected = auctionTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        // TODO: manager.startAuction(selected.getAuctionId())
    }

    /**
     * Receives live updates from AuctionManager.
     * MUST dispatch UI changes to the JavaFX Application Thread via Platform.runLater().
     */
    @Override
    public void onAuctionEvent(AuctionEvent event) {
        Platform.runLater(() -> {
            // TODO: switch on event.getType() and update auctions list / statusLabel
        });
    }
}
