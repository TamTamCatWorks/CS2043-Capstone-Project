package org.tamtamcatworks.auction.client.util.admin;

import javafx.application.Platform;
import javafx.concurrent.Task;

public final class AsyncExecutor {

  private AsyncExecutor() {}

  public static void execute(Runnable backgroundTask, Runnable onSuccess, Runnable onError) {

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

    task.setOnFailed(
        event -> {
          if (onError != null) {

            Platform.runLater(onError);
          }
        });

    Thread thread = new Thread(task);

    thread.setDaemon(true);

    thread.start();
  }
}
