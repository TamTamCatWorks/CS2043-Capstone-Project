package org.tamtamcatworks.auction.client.component.admin.sidebar;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;

import org.tamtamcatworks.auction.client.Navigation;

public class AdminSidebar
        extends VBox {

    public AdminSidebar() {

        setSpacing(10);

        setPadding(
                new Insets(20)
        );

        getStyleClass().add(
                "admin-sidebar"
        );

        Button logsButton =
                new Button("Audit Logs");

        Button reportsButton =
                new Button("Reports");

        Button dashboardButton =
                new Button("Dashboard");

        Button usersButton =
                new Button("Users");

        Button auctionsButton =
                new Button("Auctions");

        dashboardButton.setMaxWidth(
                Double.MAX_VALUE
        );

        usersButton.setMaxWidth(
                Double.MAX_VALUE
        );

        auctionsButton.setMaxWidth(
                Double.MAX_VALUE
        );

        dashboardButton.setOnAction(event ->

                Navigation.navigateTo(

                        "/fxml/admin/dashboard/admin-dashboard.fxml"
                )
        );

        usersButton.setOnAction(event ->

                Navigation.navigateTo(

                        "/fxml/admin/users/users-list.fxml"
                )
        );

        auctionsButton.setOnAction(event ->

                Navigation.navigateTo(

                        "/fxml/admin/auctions/auctions-list.fxml"
                )
        );

        reportsButton.setOnAction(event ->

                Navigation.navigateTo(

                        "/fxml/admin/reports/reports-list.fxml"
                )
        );

        logsButton.setOnAction(event ->

                Navigation.navigateTo(

                        "/fxml/admin/logs/audit-logs.fxml"
                )
        );

        getChildren().addAll(

                dashboardButton,
                usersButton,
                auctionsButton,
                reportsButton,
                logsButton
        );
    }
}