package org.tamtamcatworks.auction.client.util.async;

import javafx.application.Platform;
import javafx.concurrent.Task;

public final class AsyncHelper {

  private AsyncHelper() {}

  public static void runAsync(Runnable backgroundTask, Runnable onSuccess) {

    Task<Void> task =
        new Task<>() {

          @Override
          protected Void call() {

            backgroundTask.run();

            return null;
          }
        };

    task.setOnSucceeded(
        event -> {
          if (onSuccess != null) {
            Platform.runLater(onSuccess);
          }
        });

    new Thread(task).start();
  }
}
