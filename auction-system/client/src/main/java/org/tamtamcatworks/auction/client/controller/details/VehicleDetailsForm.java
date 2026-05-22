package org.tamtamcatworks.auction.client.controller.details;

import java.util.HashMap;
import java.util.Map;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

final class VehicleDetailsForm extends AbstractItemDetailsForm {
  private final TextField makeField = new TextField();
  private final TextField modelField = new TextField();
  private final TextField yearField = new TextField();
  private final TextField mileageKmField = new TextField();
  private final TextField colorField = new TextField();
  private final TextField fuelTypeField = new TextField();

  @Override
  public void buildForm(GridPane grid, Map<String, Object> existingDetails) {
    grid.add(createFormLabel("Make:"), 0, 0);
    grid.add(createInputGroup(makeField, "e.g. Toyota"), 1, 0);

    grid.add(createFormLabel("Model:"), 0, 1);
    grid.add(createInputGroup(modelField, "e.g. Camry"), 1, 1);

    grid.add(createFormLabel("Year:"), 0, 2);
    grid.add(createInputGroup(yearField, "e.g. 2021"), 1, 2);

    grid.add(createFormLabel("Mileage (km):"), 0, 3);
    grid.add(createInputGroup(mileageKmField, "e.g. 15000"), 1, 3);

    grid.add(createFormLabel("Color:"), 0, 4);
    grid.add(createInputGroup(colorField, "e.g. Silver"), 1, 4);

    grid.add(createFormLabel("Fuel Type:"), 0, 5);
    grid.add(createInputGroup(fuelTypeField, "e.g. Hybrid"), 1, 5);

    makeField.setText((String) existingDetails.getOrDefault("make", ""));
    modelField.setText((String) existingDetails.getOrDefault("model", ""));
    if (existingDetails.containsKey("year")) {
      yearField.setText(String.valueOf(existingDetails.get("year")));
    }
    if (existingDetails.containsKey("mileageKm")) {
      mileageKmField.setText(String.valueOf(existingDetails.get("mileageKm")));
    }
    colorField.setText((String) existingDetails.getOrDefault("color", ""));
    fuelTypeField.setText((String) existingDetails.getOrDefault("fuelType", ""));
  }

  @Override
  public Map<String, Object> toDetailsMap() {
    String make = requireText(makeField);
    String model = requireText(modelField);
    String yearStr = requireText(yearField);
    String mileageStr = requireText(mileageKmField);
    String color = requireText(colorField);
    String fuelType = requireText(fuelTypeField);

    int year = requireInt(yearStr, "Year must be a valid integer.");
    int mileage = requireNonNegativeInt(
        mileageStr,
        "Mileage must be a valid integer.",
        "Mileage cannot be negative.");

    Map<String, Object> details = new HashMap<>();
    details.put("make", make);
    details.put("model", model);
    details.put("year", year);
    details.put("mileageKm", mileage);
    details.put("color", color);
    details.put("fuelType", fuelType);
    return details;
  }
}
