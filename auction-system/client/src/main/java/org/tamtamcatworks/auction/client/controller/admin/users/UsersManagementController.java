package org.tamtamcatworks.auction.client.controller.admin.users;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.layout.VBox;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.layout.HBox;

import org.tamtamcatworks.auction.client.component.admin.feedback.Toast;
import org.tamtamcatworks.auction.client.component.admin.feedback.ToastType;
import org.tamtamcatworks.auction.client.component.admin.feedback.ErrorDialog;
import org.tamtamcatworks.auction.client.component.admin.action.AdminActionButton;
import org.tamtamcatworks.auction.client.component.admin.action.ConfirmDialog;
import org.tamtamcatworks.auction.client.Route;
import org.tamtamcatworks.auction.client.component.admin.table.PaginatedTableView;
import org.tamtamcatworks.auction.client.component.admin.table.TableColumnFactory;
import org.tamtamcatworks.auction.client.component.admin.table.TableToolbar;
import org.tamtamcatworks.auction.client.controller.BaseController;
import org.tamtamcatworks.auction.client.service.admin.AdminUserService;
import org.tamtamcatworks.auction.client.util.admin.AdminPermissionGuard;
import org.tamtamcatworks.auction.shared.response.UserResponse;

@Route(
    fxml = "/fxml/admin/users/users-list.fxml",
    layout = "/fxml/admin/layout/admin-layout.fxml"
)
public class UsersManagementController
        extends BaseController {

    @FXML
    private VBox toolbarContainer;

    @FXML
    private VBox tableContainer;

    private final AdminUserService adminUserService =
            new AdminUserService();

    private final TableToolbar toolbar =
            new TableToolbar();

    private final PaginatedTableView<UserResponse>
            paginatedTable =
            new PaginatedTableView<>();

    @FXML
    public void initialize() {

        AdminPermissionGuard.requireAdmin();

        buildToolbar();

        buildTable();

        loadUsers();
    }

    private void buildToolbar() {

        toolbarContainer.getChildren().add(
                toolbar
        );

        toolbar.getRefreshButton()
                .setOnAction(event -> loadUsers());

        toolbar.getSearchField()
                .textProperty()
                .addListener((obs, oldValue, newValue) -> {

                    searchUsers(newValue);
                });
    }

    private void buildTable() {

        var table = paginatedTable.getTableView();

        table.getColumns().addAll(

                TableColumnFactory.createStringColumn(
                        "Username",
                        UserResponse::username
                ),

                TableColumnFactory.createStringColumn(
                        "Email",
                        UserResponse::email
                ),

                TableColumnFactory.createStringColumn(
                        "Full Name",
                        UserResponse::fullName
                ),

                buildActionsColumn()
        );

        tableContainer.getChildren().add(
                paginatedTable
        );
    }

    private void loadUsers() {

        paginatedTable.showLoading();

        var users =
                adminUserService.getUsers();

        paginatedTable.getTableView().setItems(

                FXCollections.observableArrayList(
                        users
                )
        );

        paginatedTable.hideLoading();
    }

    private void searchUsers(
            String keyword
    ) {

        var users =
                adminUserService.getUsers();

        var filtered =
                users.stream()

                        .filter(user ->

                                user.username()
                                        .toLowerCase()
                                        .contains(
                                                keyword.toLowerCase()
                                        )
                        )

                        .toList();

        paginatedTable.getTableView().setItems(

                FXCollections.observableArrayList(
                        filtered
                )
        );
    }

    private TableColumn<UserResponse, Void>
    buildActionsColumn() {

        TableColumn<UserResponse, Void>
                actionsColumn =
                new TableColumn<>("Actions");

        actionsColumn.setCellFactory(column ->

                new TableCell<>() {

                    private final AdminActionButton suspendButton = new AdminActionButton("Suspend");

                    private final AdminActionButton
                        activateButton = new AdminActionButton("Activate");

                    private final HBox actionsBox =
                        new HBox(10,suspendButton,activateButton);

                    {

                        suspendButton.setOnAction(event -> {

                            UserResponse user =
                                    getTableView()
                                            .getItems()
                                            .get(getIndex());

                            boolean confirmed =
                                    ConfirmDialog.show(

                                            "Suspend User",

                                            "Suspend user: "
                                                    + user.username()
                                                    + " ?"
                                    );

                            if (confirmed) {

                                try {

                                    adminUserService
                                        .suspendUser(
                                            user.id()
                                        );

                                    Toast.show(

                                "User suspended successfully",

                                            ToastType.SUCCESS
                                    );

                                } catch (Exception ex) {

                                    ErrorDialog.show(

                                        ex.getMessage()
                                    );
                                }
                            }
                        });

                        activateButton.setOnAction(event -> {

                            UserResponse user =
                                    getTableView()
                                            .getItems()
                                            .get(getIndex());

                            boolean confirmed =
                                    ConfirmDialog.show(

                                            "Activate User",

                                            "Activate user: "
                                                    + user.username()
                                                    + " ?"
                                    );

                            if (confirmed) {

                                try {

                                    adminUserService
                                        .activateUser(
                                            user.id()
                                        );

                                    Toast.show(

                                    "User activated successfully",

                                            ToastType.SUCCESS
                                    );

                                } catch (Exception ex) {

                                    ErrorDialog.show(

                                        ex.getMessage()
                                    );
                                }
                            }
                        });
                    }

                    @Override
                    protected void updateItem(
                            Void item,
                            boolean empty
                    ) {

                        super.updateItem(item, empty);

                        if (empty) {

                            setGraphic(null);

                        } else {

                            setGraphic(actionsBox);
                        }
                    }
                }
            );

        return actionsColumn;
    }
}