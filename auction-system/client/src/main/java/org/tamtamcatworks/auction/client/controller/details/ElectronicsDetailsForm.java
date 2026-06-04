package org.tamtamcatworks.auction.client.controller.details;

import java.util.HashMap;
import java.util.Map;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

final class ElectronicsDetailsForm extends AbstractItemDetailsForm {
  private final TextField brandField = new TextField();
  private final TextField modelField = new TextField();
  private final TextField warrantyMonthsField = new TextField();

  @Override
  public void buildForm(GridPane grid, Map<String, Object> existingDetails) {
    grid.add(createFormLabel("Brand:"), 0, 0);
    grid.add(createInputGroup(brandField, "e.g. Apple"), 1, 0);

    grid.add(createFormLabel("Model:"), 0, 1);
    grid.add(createInputGroup(modelField, "e.g. iPhone 15 Pro"), 1, 1);

    grid.add(createFormLabel("Warranty Months:"), 0, 2);
    grid.add(createInputGroup(warrantyMonthsField, "e.g. 12"), 1, 2);

    brandField.setText((String) existingDetails.getOrDefault("brand", ""));
    modelField.setText((String) existingDetails.getOrDefault("model", ""));
    if (existingDetails.containsKey("warrantyMonths")) {
      warrantyMonthsField.setText(String.valueOf(existingDetails.get("warrantyMonths")));
    }
  }

  @Override
  public Map<String, Object> toDetailsMap() {
    String brand = requireText(brandField);
    String model = requireText(modelField);
    String warrantyStr = requireText(warrantyMonthsField);

    int warranty =
        requireNonNegativeInt(
            warrantyStr,
            "Warranty Months must be a valid integer.",
            "Warranty months cannot be negative.");

    Map<String, Object> details = new HashMap<>();
    details.put("brand", brand);
    details.put("model", model);
    details.put("warrantyMonths", warranty);
    return details;
  }
}
