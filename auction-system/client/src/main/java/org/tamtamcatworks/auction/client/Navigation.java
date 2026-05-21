package org.tamtamcatworks.auction.client;

import java.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/** Central navigation manager that supports layout-aware, SPA-style routing. */
public class Navigation {

  private static Stage primaryStage;
  private static Parent activeLayoutRoot;
  private static String activeLayoutPath;
  /** Direct reference to the content injection slot in the active layout. */
  private static StackPane activeContentSlot;
  /** Arbitrary context data passed between views (e.g. auction ID). */
  private static String contextData;

  /** Register the primary application stage before any navigation calls. */
  public static void setPrimaryStage(Stage stage) {
    primaryStage = stage;
  }

  /**
   * Navigate to the given FXML view path.
   * If the view belongs to a layout, the layout is loaded (or reused) and the
   * content is injected directly into the layout's slot StackPane via the
   * FXMLLoader namespace — no CSS-based lookup() required.
   */
  public static void navigateTo(String fxmlPath) {
    if (primaryStage == null) {
      throw new IllegalStateException("Primary stage not initialized in Navigation");
    }

    try {
      String requiredLayoutPath = getLayoutForPath(fxmlPath);

      if (requiredLayoutPath == null) {
        // No layout wrapper — load the page as the scene root directly
        FXMLLoader loader = new FXMLLoader(Navigation.class.getResource(fxmlPath));
        Parent root = loader.load();
        activeLayoutRoot = null;
        activeLayoutPath = null;
        activeContentSlot = null;
        setRootOnScene(root);
      } else {
        // Layout required — load or reuse the cached layout
        if (activeLayoutRoot == null || !requiredLayoutPath.equals(activeLayoutPath)) {
          FXMLLoader layoutLoader =
              new FXMLLoader(Navigation.class.getResource(requiredLayoutPath));
          activeLayoutRoot = layoutLoader.load();
          activeLayoutPath = requiredLayoutPath;

          // Resolve the slot directly from the loader namespace (no lookup needed)
          String slotId = requiredLayoutPath.contains("auth")
              ? "authContentArea"
              : "mainContentArea";
          activeContentSlot = (StackPane) layoutLoader.getNamespace().get(slotId);

          if (activeContentSlot == null) {
            throw new RuntimeException(
                "Content slot '" + slotId + "' not found in layout: " + requiredLayoutPath);
          }

          setRootOnScene(activeLayoutRoot);
        }

        // Load the content page and inject it into the cached slot
        FXMLLoader loader = new FXMLLoader(Navigation.class.getResource(fxmlPath));
        Parent pageRoot = loader.load();
        activeContentSlot.getChildren().setAll(pageRoot);
      }
    } catch (IOException e) {
      throw new RuntimeException("Failed to navigate to: " + fxmlPath, e);
    }
  }

  /** Set context data before navigating (e.g. an auction ID for the detail view). */
  public static void setContextData(String data) {
    contextData = data;
  }

  /** Retrieve context data set by the previous navigation call. */
  public static String getContextData() {
    return contextData;
  }

  private static String getLayoutForPath(String fxmlPath) {
    if (fxmlPath.contains("login.fxml") || fxmlPath.contains("register.fxml")) {
      return "/fxml/layouts/auth-layout.fxml";
    } else if (fxmlPath.contains("dashboard.fxml")
        || fxmlPath.contains("auctions-list.fxml")
        || fxmlPath.contains("auction-detail.fxml")
        || fxmlPath.contains("create-auction.fxml")) {
      return "/fxml/layouts/dashboard-layout.fxml";
    }
    return null;
  }

  private static void setRootOnScene(Parent root) {
    Scene scene = primaryStage.getScene();
    if (scene == null) {
      scene = new Scene(root, 800, 600);
      scene.getStylesheets().add(
          Launcher.class.getResource(Launcher.ASSETS_DIR + "index.css").toExternalForm());
      primaryStage.setScene(scene);
    } else {
      scene.setRoot(root);
    }
  }
}
