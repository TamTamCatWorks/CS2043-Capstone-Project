package org.tamtamcatworks.auction.client.component.admin.table;

import javafx.scene.control.Pagination;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class PaginatedTableView<T>
        extends VBox {

    private final AdminTableView<T> tableView =
            new AdminTableView<>();

    private final Pagination pagination =
            new Pagination();

    private final TableLoadingOverlay loadingOverlay =
            new TableLoadingOverlay();

    public PaginatedTableView() {

        VBox.setVgrow(
                tableView,
                Priority.ALWAYS
        );

        getChildren().addAll(

                loadingOverlay,
                tableView,
                pagination
        );

        setSpacing(10);
    }

    public AdminTableView<T> getTableView() {

        return tableView;
    }

    public Pagination getPagination() {

        return pagination;
    }

    public void showLoading() {

        loadingOverlay.setVisible(true);
    }

    public void hideLoading() {

        loadingOverlay.setVisible(false);
    }
}