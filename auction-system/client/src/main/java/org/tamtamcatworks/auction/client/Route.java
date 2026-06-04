package org.tamtamcatworks.auction.client;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares the FXML resource path and optional layout wrapper for a controller.
 *
 * <p>Register all route-annotated controllers via {@link Navigation#registerAll(Class[])} once at
 * application startup. After that, {@link Navigation#navigateTo(String)} resolves layouts
 * automatically without any string-matching code in {@code Navigation}.
 *
 * <pre>{@code
 * @Route(fxml = "/fxml/my-view.fxml", layout = Route.DASHBOARD_LAYOUT)
 * public class MyViewController { ... }
 * }</pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Route {

  /** The dashboard shell layout path. */
  String DASHBOARD_LAYOUT = "/fxml/layouts/dashboard-layout.fxml";

  /** The auth shell layout path. */
  String AUTH_LAYOUT = "/fxml/layouts/auth-layout.fxml";

  /** Sentinel for views with no layout (full-scene replacement). */
  String NONE = "";

  /** Absolute FXML resource path, e.g. {@code "/fxml/auctions-list.fxml"}. */
  String fxml();

  /**
   * Layout FXML path. Use {@link #DASHBOARD_LAYOUT}, {@link #AUTH_LAYOUT}, or {@link #NONE}
   * (default).
   */
  String layout() default NONE;
}
