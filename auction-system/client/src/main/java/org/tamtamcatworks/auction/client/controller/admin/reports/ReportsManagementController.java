package org.tamtamcatworks.auction.client.controller.admin.reports;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import org.tamtamcatworks.auction.client.Route;
import org.tamtamcatworks.auction.client.component.admin.action.AdminActionButton;
import org.tamtamcatworks.auction.client.component.admin.action.ConfirmDialog;
import org.tamtamcatworks.auction.client.component.admin.feedback.ErrorDialog;
import org.tamtamcatworks.auction.client.component.admin.feedback.Toast;
import org.tamtamcatworks.auction.client.component.admin.feedback.ToastType;
import org.tamtamcatworks.auction.client.component.admin.table.PaginatedTableView;
import org.tamtamcatworks.auction.client.component.admin.table.TableColumnFactory;
import org.tamtamcatworks.auction.client.component.admin.table.TableToolbar;
import org.tamtamcatworks.auction.client.controller.BaseController;
import org.tamtamcatworks.auction.client.service.admin.AdminReportService;
import org.tamtamcatworks.auction.client.util.admin.AdminPermissionGuard;
import org.tamtamcatworks.auction.shared.response.AdminReportResponse;

@Route(
    fxml = "/fxml/admin/reports/reports-list.fxml",
    layout = "/fxml/admin/layout/admin-layout.fxml"
)
public class ReportsManagementController
        extends BaseController {

    @FXML
    private VBox toolbarContainer;

    @FXML
    private VBox tableContainer;

    private final TableToolbar toolbar =
            new TableToolbar();

    private final PaginatedTableView<AdminReportResponse>
            paginatedTable =
            new PaginatedTableView<>();

    private final AdminReportService
            adminReportService =
            new AdminReportService();

    @FXML
    public void initialize() {

        AdminPermissionGuard.requireAdmin();

        buildToolbar();

        buildTable();

        loadReports();
    }

    private void buildToolbar() {

        toolbarContainer.getChildren().add(
                toolbar
        );

        toolbar.getRefreshButton()
                .setOnAction(event -> loadReports());
    }

    private void buildTable() {

        var table =
                paginatedTable.getTableView();

        table.getColumns().addAll(

                TableColumnFactory.createStringColumn(
                        "Type",
                        AdminReportResponse::targetType
                ),

                TableColumnFactory.createStringColumn(
                        "Target",
                        AdminReportResponse::targetName
                ),

                TableColumnFactory.createStringColumn(
                        "Reason",
                        AdminReportResponse::reason
                ),

                TableColumnFactory.createStringColumn(
                        "Status",
                        AdminReportResponse::status
                ),

                buildActionsColumn()
        );

        tableContainer.getChildren().add(
                paginatedTable
        );
    }

    private TableColumn<AdminReportResponse, Void>
    buildActionsColumn() {

        TableColumn<AdminReportResponse, Void>
                column =
                new TableColumn<>("Actions");

        column.setCellFactory(col ->

                new TableCell<>() {

                    private final AdminActionButton
                            resolveButton =
                            new AdminActionButton(
                                    "Resolve"
                            );

                    private final AdminActionButton
                            rejectButton =
                            new AdminActionButton(
                                    "Reject"
                            );

                    private final HBox actions =
                            new HBox(
                                    10,
                                    resolveButton,
                                    rejectButton
                            );

                    {

                        resolveButton.setOnAction(event -> {

                            var report =
                                    getTableView()
                                            .getItems()
                                            .get(getIndex());

                            boolean confirmed =
                                    ConfirmDialog.show(

                                            "Resolve Report",

                                            "Resolve this report?"
                                    );

                            if (confirmed) {

                                try {

                                    adminReportService
                                        .resolveReport(
                                            report.id()
                                        );

                                    Toast.show(

                                        "Report resolved successfully",

                                        ToastType.SUCCESS
                                    );

                                    loadReports();

                                } catch (Exception ex) {

                                    ErrorDialog.show(

                                        ex.getMessage()
                                    );
                                }
                            }
                        });

                        rejectButton.setOnAction(event -> {

                            var report =
                                    getTableView()
                                            .getItems()
                                            .get(getIndex());

                            boolean confirmed =
                                    ConfirmDialog.show(

                                            "Reject Report",

                                            "Reject this report?"
                                    );

                            if (confirmed) {

                                try {

                                    adminReportService
                                        .rejectReport(
                                            report.id()
                                        );

                                    Toast.show(

                                        "Report rejected successfully",

                                                ToastType.SUCCESS
                                    );

                                    loadReports();

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

                        setGraphic(
                                empty
                                        ? null
                                        : actions
                        );
                    }
                }
        );

        return column;
    }

    private void loadReports() {

        var reports =
                adminReportService
                        .getReports();

        paginatedTable.getTableView().setItems(

                FXCollections
                        .observableArrayList(
                                reports
                        )
        );
    }
}