package org.tamtamcatworks.auction.client.component.admin.sidebar;

import javafx.embed.swing.JFXPanel;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AdminSidebarTest {

    @BeforeAll
    static void initToolkit() {

        new JFXPanel();
    }

    @Test
    void sidebarShouldContainButtons() {

        AdminSidebar sidebar =
                new AdminSidebar();

        assertFalse(
                sidebar.getChildren().isEmpty()
        );
    }
}