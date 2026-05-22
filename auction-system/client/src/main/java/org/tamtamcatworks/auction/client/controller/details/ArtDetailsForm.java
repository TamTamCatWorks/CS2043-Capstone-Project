package org.tamtamcatworks.auction.client.controller.details;

import java.util.HashMap;
import java.util.Map;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

final class ArtDetailsForm extends AbstractItemDetailsForm {
  private final TextField artistField = new TextField();
  private final TextField yearCreatedField = new TextField();
  private final TextField mediumField = new TextField();
  private final TextField dimensionsField = new TextField();
  private final CheckBox hasCertificateCheckbox = new CheckBox("Has Certificate of Authenticity");

  @Override
  public void buildForm(GridPane grid, Map<String, Object> existingDetails) {
    grid.add(createFormLabel("Artist:"), 0, 0);
    grid.add(createInputGroup(artistField, "e.g. Leonardo da Vinci"), 1, 0);

    grid.add(createFormLabel("Year Created:"), 0, 1);
    grid.add(createInputGroup(yearCreatedField, "e.g. 1503"), 1, 1);

    grid.add(createFormLabel("Medium:"), 0, 2);
    grid.add(createInputGroup(mediumField, "e.g. Oil on panel"), 1, 2);

    grid.add(createFormLabel("Dimensions:"), 0, 3);
    grid.add(createInputGroup(dimensionsField, "e.g. 77 cm x 53 cm"), 1, 3);

    grid.add(hasCertificateCheckbox, 1, 4);

    artistField.setText((String) existingDetails.getOrDefault("artist", ""));
    if (existingDetails.containsKey("yearCreated")) {
      yearCreatedField.setText(String.valueOf(existingDetails.get("yearCreated")));
    }
    mediumField.setText((String) existingDetails.getOrDefault("medium", ""));
    dimensionsField.setText((String) existingDetails.getOrDefault("dimensions", ""));
    hasCertificateCheckbox.setSelected((Boolean) existingDetails.getOrDefault("hasCertificate", false));
  }

  @Override
  public Map<String, Object> toDetailsMap() {
    String artist = requireText(artistField);
    String yearStr = requireText(yearCreatedField);
    String medium = requireText(mediumField);
    String dimensions = requireText(dimensionsField);

    int year = requireInt(yearStr, "Year Created must be a valid integer.");

    Map<String, Object> details = new HashMap<>();
    details.put("artist", artist);
    details.put("yearCreated", year);
    details.put("medium", medium);
    details.put("dimensions", dimensions);
    details.put("hasCertificate", hasCertificateCheckbox.isSelected());
    return details;
  }
}
