package org.tamtamcatworks.auction.client.controller.admin.logs;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.layout.VBox;

import org.tamtamcatworks.auction.client.Route;
import org.tamtamcatworks.auction.client.component.admin.table.PaginatedTableView;
import org.tamtamcatworks.auction.client.component.admin.table.TableColumnFactory;
import org.tamtamcatworks.auction.client.component.admin.table.TableToolbar;
import org.tamtamcatworks.auction.client.controller.BaseController;
import org.tamtamcatworks.auction.client.service.admin.AdminAuditLogService;
import org.tamtamcatworks.auction.client.util.admin.AdminPermissionGuard;
import org.tamtamcatworks.auction.shared.response.AdminAuditLogResponse;

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

    @FXML
    public void initialize() {

        AdminPermissionGuard.requireAdmin();

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

        tableContainer.getChildren().add(
                paginatedTable
        );
    }

    private void loadLogs() {

        var logs =
                auditLogService.getAuditLogs();

        paginatedTable.getTableView().setItems(

                FXCollections
                        .observableArrayList(
                                logs
                        )
        );
    }
}