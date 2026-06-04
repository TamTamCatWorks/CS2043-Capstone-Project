package org.tamtamcatworks.auction.client.controller.shell;

import javafx.scene.control.Button;

/**
 * Manages tab-chip selection state for a fixed set of {@link Button}s.
 *
 * <p>Eliminates the {@code applySelected(Button, boolean)} + {@code setXxxSelected(Button)} pattern
 * that was copy-pasted across {@code DashboardController} and {@code HomeViewController}.
 *
 * <pre>{@code
 * // Create once in initialize():
 * TabChipBar tabs = new TabChipBar("profile-tab-chip-selected",
 *     menuHomeButton, menuNotificationsButton, menuTopUpButton);
 *
 * // Select a tab:
 * tabs.select(menuHomeButton);
 * }</pre>
 */
public final class TabChipBar {

  private final String selectedStyleClass;
  private final Button[] buttons;

  /**
   * Create a bar managing the given buttons.
   *
   * @param selectedStyleClass CSS class applied to the currently selected button
   * @param buttons all buttons in the bar (order does not matter)
   */
  public TabChipBar(String selectedStyleClass, Button... buttons) {
    this.selectedStyleClass = selectedStyleClass;
    this.buttons = buttons;
  }

  /**
   * Mark {@code selected} as active and remove the active class from all others. Safe to call if
   * {@code selected} is not in the original button set.
   *
   * @param selected the button to activate
   */
  public void select(Button selected) {
    for (Button btn : buttons) {
      if (btn == null) {
        continue;
      }
      btn.getStyleClass().remove(selectedStyleClass);
      if (btn == selected) {
        btn.getStyleClass().add(selectedStyleClass);
      }
    }
  }
}
