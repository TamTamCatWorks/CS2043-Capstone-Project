package org.tamtamcatworks.auction.client;

import org.tamtamcatworks.auction.client.controller.shell.LayoutController;

/**
 * Lightweight application-scoped context.
 *
 * <p>Holds the single live {@link LayoutController} instance so that child controllers (e.g. {@code
 * AuctionsListController}, {@code SearchResultsController}) can call back into the layout shell
 * without needing a direct FXML injection path.
 *
 * <p>The instance is set by {@link LayoutController#initialize()} and is guaranteed to be non-null
 * for the lifetime of any dashboard-layout child.
 */
public final class AppContext {

  private static LayoutController layoutController;

  private AppContext() {}

  /** Called once by {@link LayoutController#initialize()}. */
  public static void setLayoutController(LayoutController lc) {
    layoutController = lc;
  }

  /**
   * Returns the active {@link LayoutController}.
   *
   * @throws IllegalStateException if called before the layout has been loaded
   */
  public static LayoutController getLayoutController() {
    if (layoutController == null) {
      throw new IllegalStateException(
          "AppContext.layoutController is null — has the dashboard layout been loaded?");
    }
    return layoutController;
  }
}
