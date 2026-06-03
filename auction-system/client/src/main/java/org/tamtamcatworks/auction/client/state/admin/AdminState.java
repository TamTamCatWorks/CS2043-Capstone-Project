package org.tamtamcatworks.auction.client.state.admin;

public final class AdminState {

    private static String currentPage;

    private AdminState() {}

    public static String getCurrentPage() {
        return currentPage;
    }

    public static void setCurrentPage(String currentPage) {
        AdminState.currentPage = currentPage;
    }
}