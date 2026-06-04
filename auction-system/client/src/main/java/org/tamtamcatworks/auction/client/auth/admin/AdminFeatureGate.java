package org.tamtamcatworks.auction.client.auth.admin;

import javafx.scene.Node;

public final class AdminFeatureGate {

  private AdminFeatureGate() {}

  public static void requirePermission(Node node, AdminPermission permission) {

    boolean allowed = AdminAuthorizationService.hasPermission(permission);

    node.setVisible(allowed);

    node.setManaged(allowed);
  }
}
