package org.tamtamcatworks.auction.client;

import atlantafx.base.theme.PrimerLight;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Properties;

public class Launcher extends Application {

    static final String ASSETS_DIR       = "/assets/";
    static final String FXML_DIR         = "/fxml/";
    static final String APP_ICON_PATH    = Objects.requireNonNull(
        Launcher.class.getResource(ASSETS_DIR + "icons/app-icon.png")
    ).toExternalForm();
    static final String APP_PROPERTIES_PATH = "/application.properties";

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws IOException {
        // set AtlantaFX theme
        Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());

        // load app properties from pom.xml via filtering
        loadApplicationProperties();

        // Register routes
        Navigation.registerAll(
            org.tamtamcatworks.auction.client.controller.auth.LoginController.class,
            org.tamtamcatworks.auction.client.controller.auth.RegisterController.class,
            org.tamtamcatworks.auction.client.controller.auction.AuctionsListController.class,
            org.tamtamcatworks.auction.client.controller.auction.AuctionDetailController.class,
            org.tamtamcatworks.auction.client.controller.auction.CreateAuctionController.class,
            org.tamtamcatworks.auction.client.controller.auction.SearchResultsController.class,
            org.tamtamcatworks.auction.client.controller.shell.DashboardController.class,
            org.tamtamcatworks.auction.client.controller.admin.dashboard.AdminDashboardController.class,
            org.tamtamcatworks.auction.client.controller.admin.users.UsersManagementController.class,
            org.tamtamcatworks.auction.client.controller.admin.auctions.AuctionsManagementController.class,
            org.tamtamcatworks.auction.client.controller.admin.reports.ReportsManagementController.class,
            org.tamtamcatworks.auction.client.controller.admin.logs.AuditLogsController.class
        );

        // Set Navigation stage
        Navigation.setPrimaryStage(stage);

        stage.setTitle(System.getProperty("app.name", "Auction System"));
        stage.getIcons().add(new Image(APP_ICON_PATH));
        stage.setOnCloseRequest(t -> Platform.exit());
        Platform.runLater(() -> {
            stage.show();
            stage.setMaximized(true);
            stage.requestFocus();
        });

        // Load initial view — login screen using layout routing
        Navigation.navigateTo(
            FXML_DIR + "login.fxml");

        Platform.runLater(() -> {
            stage.show();
            stage.requestFocus();
        });
    }

    private void loadApplicationProperties() {
        try {
            Properties properties = new Properties();
            properties.load(new InputStreamReader(
                Objects.requireNonNull(getClass().getResourceAsStream(APP_PROPERTIES_PATH)),
                StandardCharsets.UTF_8
            ));
            properties.forEach((key, value) -> System.setProperty(
                String.valueOf(key),
                String.valueOf(value)
            ));
        } catch (IOException e) {
            throw new RuntimeException("Failed to load application properties", e);
        }
    }
}
