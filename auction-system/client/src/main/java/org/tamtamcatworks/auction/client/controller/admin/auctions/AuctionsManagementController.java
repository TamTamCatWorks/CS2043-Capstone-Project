package org.tamtamcatworks.auction.client.controller.admin.auctions;

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
import org.tamtamcatworks.auction.client.service.admin.AdminAuctionService;
import org.tamtamcatworks.auction.client.util.admin.AdminPermissionGuard;
import org.tamtamcatworks.auction.shared.response.AuctionResponse;

@Route(
    fxml = "/fxml/admin/auctions/auctions-list.fxml",
    layout = "/fxml/admin/layout/admin-layout.fxml"
)
public class AuctionsManagementController
        extends BaseController {

    @FXML
    private VBox toolbarContainer;

    @FXML
    private VBox tableContainer;

    private final TableToolbar toolbar =
            new TableToolbar();

    private final PaginatedTableView<AuctionResponse>
            paginatedTable =
            new PaginatedTableView<>();

    private final AdminAuctionService
            adminAuctionService =
            new AdminAuctionService();

    @FXML
    public void initialize() {

        AdminPermissionGuard.requireAdmin();

        buildToolbar();

        buildTable();

        loadAuctions();
    }

    private void buildToolbar() {

        toolbarContainer.getChildren().add(
                toolbar
        );

        toolbar.getRefreshButton()
                .setOnAction(event -> loadAuctions());
    }

    private void buildTable() {

        var table =
                paginatedTable.getTableView();

        table.getColumns().addAll(

                TableColumnFactory.createStringColumn(
                        "Title",
                        AuctionResponse::title
                ),

                TableColumnFactory.createStringColumn(
                        "Status",
                        AuctionResponse::status
                ),

                buildActionsColumn()
        );

        tableContainer.getChildren().add(
                paginatedTable
        );
    }

    private TableColumn<AuctionResponse, Void>
    buildActionsColumn() {

        TableColumn<AuctionResponse, Void>
                actionsColumn =
                new TableColumn<>("Actions");

        actionsColumn.setCellFactory(column ->

                new TableCell<>() {

                    private final AdminActionButton
                            closeButton =
                            new AdminActionButton(
                                    "Close"
                            );

                    private final HBox actionsBox =
                            new HBox(
                                    10,
                                    closeButton
                            );

                    {

                        closeButton.setOnAction(event -> {

                            AuctionResponse auction =
                                    getTableView()
                                            .getItems()
                                            .get(getIndex());

                            boolean confirmed =
                                    ConfirmDialog.show(

                                            "Close Auction",

                                            "Close auction: "
                                                    + auction.title()
                                                    + " ?"
                                    );

                            if (confirmed) {

                                try {

                                        adminAuctionService
                                                .closeAuction(
                                                        auction.id()
                                                );

                                        Toast.show(

                                                "Auction closed successfully",

                                                        ToastType.SUCCESS
                                        );

                                        loadAuctions();

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

    private void loadAuctions() {

        paginatedTable.showLoading();

        var auctions =
                adminAuctionService
                        .getAuctions();

        paginatedTable.getTableView().setItems(

                FXCollections.observableArrayList(
                        auctions
                )
        );

        paginatedTable.hideLoading();
    }
}