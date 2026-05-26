package org.tamtamcatworks.auction.client;

import java.util.concurrent.Callable;
import java.util.function.Consumer;
import javafx.application.Platform;
import javafx.concurrent.Task;

/**
 * Fluent helper that wraps {@link Task} boilerplate into a single-line call.
 *
 * <pre>{@code
 * AsyncTask.run(apiClient::getAllAuctions)
 *     .onSuccess(auctions -> listView.getItems().setAll(auctions))
 *     .onFailure(ex -> showError(ex.getMessage()))
 *     .start();
 * }</pre>
 *
 * <p>Both {@code onSuccess} and {@code onFailure} callbacks are always
 * invoked on the JavaFX Application Thread.
 *
 * @param <T> the result type produced by the background callable
 */
public final class AsyncTask<T> {

  private final Callable<T> work;
  private Consumer<T> successHandler = result -> { };
  private Consumer<Throwable> failureHandler = ex -> { };
  private Runnable finallyHandler = () -> { };

  private AsyncTask(Callable<T> work) {
    this.work = work;
  }

  /**
   * Create a new {@code AsyncTask} from a background {@link Callable}.
   *
   * @param <T>  result type
   * @param work the background operation to run
   * @return a new {@code AsyncTask} (not yet started)
   */
  public static <T> AsyncTask<T> run(Callable<T> work) {
    return new AsyncTask<>(work);
  }

  /**
   * Register a callback to be invoked (on the FX thread) when the task
   * completes successfully.
   *
   * @param handler consumer of the result value
   * @return {@code this} for chaining
   */
  public AsyncTask<T> onSuccess(Consumer<T> handler) {
    this.successHandler = handler;
    return this;
  }

  /**
   * Register a callback to be invoked (on the FX thread) when the task
   * fails with an exception.
   *
   * @param handler consumer of the thrown exception
   * @return {@code this} for chaining
   */
  public AsyncTask<T> onFailure(Consumer<Throwable> handler) {
    this.failureHandler = handler;
    return this;
  }

  /**
   * Register a callback to be invoked on the FX thread after either success
   * or failure (like a {@code finally} block).
   *
   * @param handler runnable to always execute
   * @return {@code this} for chaining
   */
  public AsyncTask<T> always(Runnable handler) {
    this.finallyHandler = handler;
    return this;
  }

  /**
   * Start the background task on a new daemon thread.
   * The returned {@link Task} can be used to cancel execution if needed.
   *
   * @return the underlying {@link Task} (already started)
   */
  public Task<T> start() {
    Task<T> task = new Task<>() {
      @Override
      protected T call() throws Exception {
        return work.call();
      }
    };

    final Consumer<T> onSuccess = successHandler;
    final Consumer<Throwable> onFailure = failureHandler;
    final Runnable always = finallyHandler;

    task.setOnSucceeded(e -> Platform.runLater(() -> {
      onSuccess.accept(task.getValue());
      always.run();
    }));

    task.setOnFailed(e -> Platform.runLater(() -> {
      onFailure.accept(task.getException());
      always.run();
    }));

    Thread thread = new Thread(task, "async-task");
    thread.setDaemon(true);
    thread.start();
    return task;
  }
}
