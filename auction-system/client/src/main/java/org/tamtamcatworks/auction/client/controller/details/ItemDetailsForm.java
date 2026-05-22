package org.tamtamcatworks.auction.client.controller.details;

import java.util.Map;
import javafx.scene.layout.GridPane;

public interface ItemDetailsForm {
  void buildForm(GridPane grid, Map<String, Object> existingDetails);

  Map<String, Object> toDetailsMap();
}
