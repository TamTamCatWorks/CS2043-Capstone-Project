package org.tamtamcatworks.auction.client.controller.admin.logs;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.layout.VBox;

import org.tamtamcatworks.auction.client.Route;
import org.tamtamcatworks.auction.client.auth.admin.AdminAuthorizationService;
import org.tamtamcatworks.auction.client.auth.admin.AdminPermission;
import org.tamtamcatworks.auction.client.component.admin.table.PaginatedTableView;
import org.tamtamcatworks.auction.client.component.admin.table.TableColumnFactory;
import org.tamtamcatworks.auction.client.component.admin.table.TableToolbar;
import org.tamtamcatworks.auction.client.controller.BaseController;
import org.tamtamcatworks.auction.client.service.admin.AdminAuditLogService;
import org.tamtamcatworks.auction.shared.response.AdminAuditLogResponse;
import org.tamtamcatworks.auction.client.component.admin.feedback.LoadingOverlay;
import org.tamtamcatworks.auction.client.util.admin.AsyncExecutor;

import org.tamtamcatworks.auction.client.component.admin.feedback.Toast;
import org.tamtamcatworks.auction.client.component.admin.feedback.ToastType;
import org.tamtamcatworks.auction.client.component.admin.feedback.ErrorDialog;

@Route(
    fxml = "/fxml/admin/logs/audit-logs.fxml",
    layout = "/fxml/admin/layout/admin-layout.fxml"
)
public class AuditLogsController
        extends BaseController {

    @FXML
    private VBox toolbarContainer;

    @FXML
    private VBox tableContainer;

    private final TableToolbar toolbar =
            new TableToolbar();

    private final PaginatedTableView<AdminAuditLogResponse>
            paginatedTable =
            new PaginatedTableView<>();

    private final AdminAuditLogService
            auditLogService =
            new AdminAuditLogService();

    private final LoadingOverlay
            loadingOverlay =
            new LoadingOverlay();

    @FXML
    public void initialize() {

        if (!AdminAuthorizationService.hasPermission(AdminPermission.USER_MANAGE)) {

                throw new RuntimeException(
                "Access denied"
                );
        }

        buildToolbar();

        buildTable();

        loadLogs();
    }

    private void buildToolbar() {

        toolbarContainer.getChildren().add(
                toolbar
        );

        toolbar.getRefreshButton()
                .setOnAction(event -> loadLogs());
    }

    private void buildTable() {

        var table =
                paginatedTable.getTableView();

        table.getColumns().addAll(

                TableColumnFactory.createStringColumn(
                        "Admin",
                        AdminAuditLogResponse::adminName
                ),

                TableColumnFactory.createStringColumn(
                        "Action",
                        AdminAuditLogResponse::action
                ),

                TableColumnFactory.createStringColumn(
                        "Target",
                        AdminAuditLogResponse::target
                ),

                TableColumnFactory.createStringColumn(
                        "Timestamp",
                        log -> log.timestamp().toString()
                )
        );

        tableContainer.getChildren().addAll(
                paginatedTable,
                loadingOverlay
        );
    }

    private void loadLogs() {

        loadingOverlay.show();

        AsyncExecutor.execute(

                () -> {

                        var logs =
                                auditLogService
                                        .getAuditLogs();

                        javafx.application.Platform.runLater(() ->

                                paginatedTable
                                        .getTableView()
                                        .setItems(

                                                FXCollections
                                                        .observableArrayList(
                                                                logs
                                                        )
                                        )
                        );
                },

                () -> {

                        loadingOverlay.hide();

                        Toast.show(

                                "Audit logs loaded",

                                ToastType.INFO
                        );
                },

                () -> {

                        loadingOverlay.hide();

                        ErrorDialog.show(

                                "Failed to load audit logs"
                        );
                }
                );
        }
}