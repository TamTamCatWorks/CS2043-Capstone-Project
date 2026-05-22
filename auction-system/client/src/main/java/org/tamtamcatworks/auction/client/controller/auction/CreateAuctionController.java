package org.tamtamcatworks.auction.client.controller.auction;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import com.dlsc.gemsfx.TimePicker;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import org.tamtamcatworks.auction.client.Navigation;
import org.tamtamcatworks.auction.client.SessionManager;
import org.tamtamcatworks.auction.client.controller.details.ItemDetailsForm;
import org.tamtamcatworks.auction.client.controller.details.ItemDetailsFormFactory;
import org.tamtamcatworks.auction.client.controller.details.ItemDetailsType;
import org.tamtamcatworks.auction.shared.request.CreateAuctionRequest;
import org.tamtamcatworks.auction.shared.request.ItemRequest;
import org.tamtamcatworks.auction.shared.response.AuctionResponse;

/** Controller for the create auction form. */
public class CreateAuctionController {

  @FXML private TextField titleField;
  @FXML private TextField itemNameField;
  @FXML private TextArea itemDescField;
  @FXML private ComboBox<String> itemTypeCombo;
  @FXML private ComboBox<String> conditionCombo;
  @FXML private VBox detailsSectionBox;
  @FXML private GridPane detailsGrid;
  @FXML private TextField startingPriceField;
  @FXML private DatePicker startDatePicker;
  @FXML private TimePicker startTimePicker;
  @FXML private ComboBox<Integer> durationDaysCombo;
  @FXML private Label messageLabel;
  @FXML private Button createButton;
  @FXML private ProgressIndicator progressIndicator;

  // Image upload elements
  @FXML private ImageView imagePreview;
  @FXML private Label imagePlaceholderLabel;
  @FXML private Label imagePathLabel;

  private File selectedImageFile;
  private final Map<String, Object> detailsMap = new HashMap<>();
  private String currentItemType = null;
  private ItemDetailsForm currentDetailsForm;

  @FXML
  public void initialize() {
    // Only add item types supported by backend's ItemType enum
    itemTypeCombo.getItems().addAll("Art", "Electronics", "Vehicle", "Other");
    conditionCombo.getItems().addAll(
        "New", "Like New", "Excellent", "Good", "Fair", "Poor"
    );
    durationDaysCombo.getItems().addAll(1, 3, 5, 7, 10);
    durationDaysCombo.setValue(7);
    itemTypeCombo.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
      handleItemTypeChange(newVal);
    });
    messageLabel.setVisible(false);
    messageLabel.setManaged(false);
    progressIndicator.setVisible(false);
  }

  @FXML
  private void handleChooseImage() {
    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle("Select Item Image");
    fileChooser.getExtensionFilters().addAll(
        new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
    );
    File file = fileChooser.showOpenDialog(titleField.getScene().getWindow());
    if (file != null) {
      selectedImageFile = file;
      imagePathLabel.setText(file.getName());
      try {
        Image img = new Image(file.toURI().toURL().toExternalForm());
        imagePreview.setImage(img);
        imagePlaceholderLabel.setVisible(false);
      } catch (Exception e) {
        showError("Failed to load image preview.");
      }
    }
  }

  @FXML
  private void handleCreateAuction() {
    // Validate required fields
    String title = titleField.getText().trim();
    String itemName = itemNameField.getText().trim();
    String itemDesc = itemDescField.getText().trim();
    String itemType = itemTypeCombo.getValue();
    String condition = conditionCombo.getValue();
    String priceText = startingPriceField.getText().trim();

    if (title.isEmpty() || itemName.isEmpty() || priceText.isEmpty()) {
      showError("Please fill in auction title, item name, and starting price.");
      return;
    }

    if (itemType == null) {
      showError("Please select an item type.");
      return;
    }

    if (condition == null) {
      showError("Please select item condition.");
      return;
    }

    if (selectedImageFile == null) {
      showError("Please select an item image to upload.");
      return;
    }

    double startingPrice;
    try {
      startingPrice = Double.parseDouble(priceText);
      if (startingPrice <= 0) {
        showError("Starting price must be greater than zero.");
        return;
      }
    } catch (NumberFormatException e) {
      showError("Please enter a valid starting price.");
      return;
    }

    LocalDateTime startTime = parseOptionalStartDateTime();
    if (startTime == null) {
      return; // error already shown
    }

    Integer durationDays = parseDurationDays();
    if (durationDays == null) {
      return;
    }

    if (!prepareItemDetails(itemType)) {
      return;
    }

    LocalDateTime endTime = startTime.plusDays(durationDays);

    setLoading(true);
    hideMessage();

    String sellerId = SessionManager.getCurrentUser() != null
        ? SessionManager.getCurrentUser().id() : "";

    // Upload image first, then submit auction creation
    Task<AuctionResponse> task = new Task<>() {
      @Override
      protected AuctionResponse call() throws Exception {
        String uploadedUrl = SessionManager.getApiClient().uploadImage(selectedImageFile);
        if (uploadedUrl == null) {
          throw new RuntimeException("Image upload failed");
        }

        ItemRequest itemRequest = new ItemRequest(
            itemType, itemName, itemDesc, startingPrice,
            condition, sellerId, uploadedUrl, detailsMap
        );

        CreateAuctionRequest request = new CreateAuctionRequest(
            title, itemRequest, startTime, endTime
        );

        return SessionManager.getApiClient().createAuctionWithItem(request);
      }
    };

    task.setOnSucceeded(e -> {
      setLoading(false);
      AuctionResponse result = task.getValue();
      if (result != null) {
        showSuccess("Auction created successfully!");
        // Navigate to the new auction detail after a brief delay
        javafx.application.Platform.runLater(() -> {
          Navigation.setContextData(result.id());
          Navigation.navigateTo("/fxml/auction-detail.fxml");
        });
      }
    });

    task.setOnFailed(e -> {
      setLoading(false);
      Throwable ex = task.getException();
      showError("Failed to create auction: "
          + (ex != null && ex.getMessage() != null ? ex.getMessage() : "Unknown error"));
    });

    new Thread(task).start();
  }

  private LocalDateTime parseOptionalStartDateTime() {
    LocalDate selectedDate = startDatePicker.getValue();
    LocalTime selectedTime = startTimePicker.getTime();

    if (selectedDate == null && selectedTime == null) {
      return LocalDateTime.now();
    }

    if (selectedDate == null || selectedTime == null) {
      showError("Please select both start date and time, or leave both blank to start now.");
      return null;
    }

    return LocalDateTime.of(selectedDate, selectedTime);
  }

  private Integer parseDurationDays() {
    Integer durationDays = durationDaysCombo.getValue();
    if (durationDays == null) {
      showError("Please select an auction duration.");
      return null;
    }
    return durationDays;
  }

  private boolean prepareItemDetails(String itemTypeLabel) {
    ItemDetailsType type = ItemDetailsType.fromDisplayName(itemTypeLabel);
    if (type == null || !type.requiresDetails()) {
      detailsMap.clear();
      return true;
    }

    if (currentDetailsForm == null) {
      showError("Please provide item-specific details.");
      return false;
    }

    try {
      Map<String, Object> updatedDetails = currentDetailsForm.toDetailsMap();
      detailsMap.clear();
      detailsMap.putAll(updatedDetails);
      return true;
    } catch (IllegalArgumentException e) {
      showError(e.getMessage());
      return false;
    }
  }

  private void showError(String message) {
    messageLabel.getStyleClass().removeAll("success-label");
    if (!messageLabel.getStyleClass().contains("error-label")) {
      messageLabel.getStyleClass().add("error-label");
    }
    messageLabel.setText(message);
    messageLabel.setVisible(true);
    messageLabel.setManaged(true);
  }

  private void showSuccess(String message) {
    messageLabel.getStyleClass().removeAll("error-label");
    if (!messageLabel.getStyleClass().contains("success-label")) {
      messageLabel.getStyleClass().add("success-label");
    }
    messageLabel.setText(message);
    messageLabel.setVisible(true);
    messageLabel.setManaged(true);
  }

  private void hideMessage() {
    messageLabel.setVisible(false);
    messageLabel.setManaged(false);
  }

  private void setLoading(boolean loading) {
    createButton.setDisable(loading);
    progressIndicator.setVisible(loading);
  }

  private void handleItemTypeChange(String type) {
    if (type == null) {
      currentItemType = null;
      detailsMap.clear();
      currentDetailsForm = null;
      hideInlineDetails();
      return;
    }

    if (!type.equals(currentItemType)) {
      detailsMap.clear();
      currentItemType = type;
      currentDetailsForm = null;
      renderInlineDetails(type);
    }
  }

  private void renderInlineDetails(String typeLabel) {
    ItemDetailsType type = ItemDetailsType.fromDisplayName(typeLabel);
    if (type == null || !type.requiresDetails()) {
      hideInlineDetails();
      return;
    }

    ItemDetailsForm detailsForm = ItemDetailsFormFactory.create(type);
    if (detailsForm == null) {
      hideInlineDetails();
      return;
    }

    detailsGrid.getChildren().clear();
    detailsForm.buildForm(detailsGrid, detailsMap);
    currentDetailsForm = detailsForm;
    detailsSectionBox.setVisible(true);
    detailsSectionBox.setManaged(true);
  }

  private void hideInlineDetails() {
    detailsGrid.getChildren().clear();
    detailsSectionBox.setVisible(false);
    detailsSectionBox.setManaged(false);
  }
}
