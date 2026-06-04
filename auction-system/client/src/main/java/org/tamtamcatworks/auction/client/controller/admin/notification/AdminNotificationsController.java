package org.tamtamcatworks.auction.client.controller.admin.notification;

import org.tamtamcatworks.auction.client.Route;
import org.tamtamcatworks.auction.client.component.admin.table.PaginatedTableView;
import org.tamtamcatworks.auction.client.component.admin.table.TableColumnFactory;
import org.tamtamcatworks.auction.client.controller.BaseController;
import org.tamtamcatworks.auction.client.service.admin.AdminNotificationService;
import org.tamtamcatworks.auction.shared.response.NotificationResponse;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.layout.VBox;

@Route(
        fxml = "/fxml/admin/notification/admin-notifications.fxml",
    layout = "/fxml/admin/layout/admin-layout.fxml"
)
public class AdminNotificationsController
        extends BaseController {

    @FXML
    private VBox tableContainer;

    private final AdminNotificationService service =
            new AdminNotificationService();

    private final PaginatedTableView<NotificationResponse>
            table =
            new PaginatedTableView<>();

    @FXML
    public void initialize() {

        buildTable();

        loadNotifications();
    }

    private void buildTable() {

        table.getTableView()
             .getColumns()
             .addAll(

                TableColumnFactory.createStringColumn(
                        "Type",
                        NotificationResponse::type
                ),

                TableColumnFactory.createStringColumn(
                        "Message",
                        NotificationResponse::message
                ),

                TableColumnFactory.createStringColumn(
                        "Created",
                        NotificationResponse::createdAt
                )
        );

        tableContainer.getChildren()
                .add(table);
    }

    private void loadNotifications() {

        table.getTableView()
             .setItems(

                FXCollections.observableArrayList(
                        service.getNotifications()
                )
        );
    }
}