package auction.ui;

import javafx.application.Application;
import javafx.stage.Stage;

/**
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │  📌 CONCEPT: JavaFX Application Lifecycle                              │
 * │                                                                         │
 * │  JavaFX applications extend javafx.application.Application and          │
 * │  override start(Stage). The Stage is the top-level window; a Scene      │
 * │  holds the scenegraph (your UI hierarchy).                             │
 * │                                                                         │
 * │  Lifecycle methods:                                                     │
 * │    init()  — called before start() on the launcher thread              │
 * │    start() — UI setup; called on the JavaFX Application Thread         │
 * │    stop()  — cleanup when the window closes                            │
 * │                                                                         │
 * │  STUDENT QUESTIONS:                                                     │
 * │                                                                         │
 * │  Q1. Why must all UI updates happen on the JavaFX Application Thread   │
 * │      (JAT)? What exception is thrown if you update a Node from a       │
 * │      background thread? How does Platform.runLater() solve this?        │
 * │                                                                         │
 * │  Q2. Your AuctionObserver implementations will be called from the       │
 * │      same thread that called AuctionManager methods. If you call        │
 * │      AuctionManager from a background thread (e.g. a Timer), what       │
 * │      must you do before updating any JavaFX control?                   │
 * │                                                                         │
 * │  Q3. Sketch a minimal scene graph for the auction list screen:          │
 * │      Stage → Scene → BorderPane →                                       │
 * │        TOP: toolbar with "Create Auction" button                        │
 * │        CENTER: TableView<Auction> with columns for item, price, status │
 * │        BOTTOM: status bar Label                                         │
 * │      Which JavaFX layout pane fits best for each area?                 │
 * │                                                                         │
 * │  📌 SPRING POINTER                                                      │
 * │  Integrating Spring Boot with JavaFX requires launching Spring's       │
 * │  ApplicationContext inside init() and then pulling beans from it.      │
 * │  A popular library for this is fx-weaver or SpringFX.                  │
 * │  You would annotate controllers with @Component and @Autowired          │
 * │  AuctionManager (as a Spring bean) rather than calling getInstance().  │
 * └─────────────────────────────────────────────────────────────────────────┘
 */
public class MainApp extends Application {

    @Override
    public void init() throws Exception {
        // TODO: Optional pre-start setup (e.g. load saved state, initialise Spring context)
    }

    /**
     * Entry point for the JavaFX UI.
     * Load your first FXML file or build the scene programmatically here.
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        // TODO: Load FXML or construct scene
        //   Option A (FXML):
        //     FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main.fxml"));
        //     Parent root = loader.load();
        //     primaryStage.setScene(new Scene(root, 900, 600));
        //
        //   Option B (programmatic):
        //     VBox root = new VBox(new Label("Auction System"));
        //     primaryStage.setScene(new Scene(root, 900, 600));

        primaryStage.setTitle("Auction System");
        primaryStage.show();

        throw new UnsupportedOperationException("UI not yet implemented");
    }

    @Override
    public void stop() throws Exception {
        // TODO: cleanup resources if needed
    }

    /**
     * Standard Java entry point. For JavaFX 11+ this must call launch().
     */
    public static void main(String[] args) {
        launch(args);
    }
}
