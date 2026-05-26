package org.tamtamcatworks.auction.client;

import java.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;

/**
 * Fluent helper for loading an FXML view and injecting it into a
 * {@link StackPane} slot.
 *
 * <p>Eliminates the private {@code loadView(String)} method that was
 * copy-pasted across {@code DashboardController} and
 * {@code HomeViewController}.
 *
 * <pre>{@code
 * // Before (repeated in every controller):
 * private void loadView(String path) {
 *     try {
 *         FXMLLoader loader = new FXMLLoader(getClass().getResource(path));
 *         Parent view = loader.load();
 *         rightContentArea.getChildren().setAll(view);
 *     } catch (Exception ex) { ex.printStackTrace(); }
 * }
 *
 * // After:
 * ViewLoader.into(rightContentArea).load("/fxml/dashboard/home.fxml");
 * }</pre>
 */
public final class ViewLoader {

  private final StackPane target;

  private ViewLoader(StackPane target) {
    this.target = target;
  }

  /**
   * Begin a load operation targeting the given {@code StackPane}.
   *
   * @param target the pane that will receive the loaded view
   * @return a {@code ViewLoader} bound to {@code target}
   */
  public static ViewLoader into(StackPane target) {
    return new ViewLoader(target);
  }

  /**
   * Load the FXML at the given classpath resource path and inject it into the
   * bound target pane, replacing any existing children.
   *
   * @param fxmlPath absolute classpath resource path
   *                 (e.g. {@code "/fxml/dashboard/home.fxml"})
   * @throws RuntimeException if the FXML cannot be loaded
   */
  public void load(String fxmlPath) {
    try {
      FXMLLoader loader = new FXMLLoader(ViewLoader.class.getResource(fxmlPath));
      Parent view = loader.load();
      target.getChildren().setAll(view);
    } catch (IOException ex) {
      throw new RuntimeException("Failed to load view: " + fxmlPath, ex);
    }
  }

  /**
   * Load the FXML and return the controller instance for further
   * configuration before the view is displayed.
   *
   * @param <C>      controller type
   * @param fxmlPath absolute classpath resource path
   * @return the controller associated with the loaded FXML
   * @throws RuntimeException if the FXML cannot be loaded
   */
  public <C> C loadWithController(String fxmlPath) {
    try {
      FXMLLoader loader = new FXMLLoader(ViewLoader.class.getResource(fxmlPath));
      Parent view = loader.load();
      target.getChildren().setAll(view);
      return loader.getController();
    } catch (IOException ex) {
      throw new RuntimeException("Failed to load view: " + fxmlPath, ex);
    }
  }
}
