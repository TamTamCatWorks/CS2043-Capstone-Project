package org.tamtamcatworks.auction.client.controller.details;

import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

abstract class AbstractItemDetailsForm implements ItemDetailsForm {

  protected HBox createInputGroup(TextField field, String promptText) {
    field.setPromptText(promptText);
    field.getStyleClass().add("edge-to-edge-field");
    HBox inputGroup = new HBox(field);
    inputGroup.getStyleClass().add("input-group");
    return inputGroup;
  }

  protected Label createFormLabel(String text) {
    Label label = new Label(text);
    label.getStyleClass().add("form-label");
    return label;
  }

  protected String requireText(TextField field) {
    String value = field.getText().trim();
    if (value.isEmpty()) {
      throw new IllegalArgumentException("All fields are required.");
    }
    return value;
  }

  protected int requireInt(String value, String errorMessage) {
    try {
      return Integer.parseInt(value);
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException(errorMessage);
    }
  }

  protected int requireNonNegativeInt(String value, String invalidMessage, String negativeMessage) {
    int parsed = requireInt(value, invalidMessage);
    if (parsed < 0) {
      throw new IllegalArgumentException(negativeMessage);
    }
    return parsed;
  }
}
