package org.tamtamcatworks.auction.client.component.admin.table;

import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.TableColumn;

import java.util.function.Function;

public final class TableColumnFactory {

    private TableColumnFactory() {}

    public static <T> TableColumn<T, String>
    createStringColumn(
            String title,
            Function<T, String> mapper
    ) {

        TableColumn<T, String> column =
                new TableColumn<>(title);

        column.setCellValueFactory(cellData ->

                new SimpleStringProperty(
                        mapper.apply(
                                cellData.getValue()
                        )
                )
        );

        return column;
    }
}