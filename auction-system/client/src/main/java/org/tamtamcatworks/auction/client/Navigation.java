package org.tamtamcatworks.auction.client;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * Central navigation manager — layout-aware SPA-style routing.
 *
 * <h3>Route registration</h3>
 *
 * <p>Routes are registered at startup via {@link #registerAll(Class[])} which reads the {@link
 * Route} annotation from each controller class. After that, {@link #navigateTo(String)} resolves
 * the correct layout automatically — <strong>no edits to this class are needed when adding a new
 * view</strong>.
 *
 * <pre>{@code
 * // In Launcher.start():
 * Navigation.registerAll(
 *     LoginController.class,
 *     AuctionsListController.class,
 *     // … all controller classes
 * );
 * }</pre>
 *
 * <h3>Passing context between views</h3>
 *
 * <pre>{@code
 * Navigation.setContextData(auction.id());
 * Navigation.navigateTo("/fxml/auction-detail.fxml");
 * }</pre>
 */
public class Navigation {

  private static Stage primaryStage;
  private static Parent activeLayoutRoot;
  private static String activeLayoutPath;

  /** Direct reference to the content slot in the active layout. */
  private static StackPane activeContentSlot;

  /** Arbitrary context passed between views (e.g. an auction ID). */
  private static String contextData;

  /**
   * Map from FXML path → layout path, populated by {@link #registerAll}. A {@code null} layout
   * value means no layout (full-scene replacement).
   */
  private static final Map<String, String> routeMap = new HashMap<>();

  /** Register the primary application stage before any navigation calls. */
  public static void setPrimaryStage(Stage stage) {
    primaryStage = stage;
  }

  // ── Route registration ────────────────────────────────────────────────────

  /**
   * Register a single route explicitly.
   *
   * @param fxmlPath the view's classpath resource path
   * @param layoutPath the required layout path, or {@code null} / {@code ""} for a full-scene
   *     (no-layout) view
   */
  public static void register(String fxmlPath, String layoutPath) {
    routeMap.put(fxmlPath, layoutPath == null ? "" : layoutPath);
  }

  /**
   * Scan all provided controller classes for a {@link Route} annotation and register each one.
   * Controllers without the annotation are silently skipped.
   *
   * @param controllerClasses controller classes to inspect
   */
  public static void registerAll(Class<?>... controllerClasses) {
    for (Class<?> cls : controllerClasses) {
      Route route = cls.getAnnotation(Route.class);
      if (route != null) {
        routeMap.put(route.fxml(), route.layout());
      }
    }
  }

  // ── Navigation ────────────────────────────────────────────────────────────

  /**
   * Navigate to the given FXML view. If the view has a registered layout the layout is loaded (or
   * reused) and the content injected into its slot.
   *
   * @param fxmlPath classpath resource path to the target view
   * @throws RuntimeException if the FXML cannot be loaded
   */
  public static void navigateTo(String fxmlPath) {
    if (primaryStage == null) {
      throw new IllegalStateException(
          "Primary stage not set — call Navigation.setPrimaryStage() first.");
    }

    try {
      String requiredLayout = getLayoutForPath(fxmlPath);

      if (requiredLayout == null || requiredLayout.isBlank()) {
        // Full-scene replacement — no layout wrapper
        FXMLLoader loader = new FXMLLoader(Navigation.class.getResource(fxmlPath));
        Parent root = loader.load();
        activeLayoutRoot = null;
        activeLayoutPath = null;
        activeContentSlot = null;
        setRootOnScene(root);

      } else {
        // Load or reuse the layout shell
        if (activeLayoutRoot == null || !requiredLayout.equals(activeLayoutPath)) {
          FXMLLoader layoutLoader = new FXMLLoader(Navigation.class.getResource(requiredLayout));
          activeLayoutRoot = layoutLoader.load();
          activeLayoutPath = requiredLayout;

          String slotId = requiredLayout.contains("auth") ? "authContentArea" : "mainContentArea";
          activeContentSlot = (StackPane) layoutLoader.getNamespace().get(slotId);

          if (activeContentSlot == null) {
            throw new RuntimeException(
                "Content slot '" + slotId + "' not found in layout: " + requiredLayout);
          }

          setRootOnScene(activeLayoutRoot);
        }

        // Inject the content view
        FXMLLoader loader = new FXMLLoader(Navigation.class.getResource(fxmlPath));
        Parent pageRoot = loader.load();
        activeContentSlot.getChildren().setAll(pageRoot);
      }

    } catch (IOException e) {
      throw new RuntimeException("Failed to navigate to: " + fxmlPath, e);
    }
  }

  // ── Context data ──────────────────────────────────────────────────────────

  /** Set context data before navigating (e.g. an auction ID for the detail view). */
  public static void setContextData(String data) {
    contextData = data;
  }

  /** Retrieve context data set by the previous navigation call. */
  public static String getContextData() {
    return contextData;
  }

  // ── Private helpers ───────────────────────────────────────────────────────

  /**
   * Resolve the required layout for {@code fxmlPath} using the registered route map. Falls back to
   * {@code null} (no layout) for unknown paths.
   */
  private static String getLayoutForPath(String fxmlPath) {
    return routeMap.getOrDefault(fxmlPath, null);
  }

  private static void setRootOnScene(Parent root) {
    Scene scene = primaryStage.getScene();
    if (scene == null) {
      scene = new Scene(root, 800, 600);
      scene
          .getStylesheets()
          .add(Launcher.class.getResource(Launcher.ASSETS_DIR + "index.css").toExternalForm());
      primaryStage.setScene(scene);
    } else {
      scene.setRoot(root);
    }
  }
}
