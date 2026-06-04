package org.tamtamcatworks.auction.client.exception;

public class AdminApiException extends RuntimeException {

  public AdminApiException(String message) {

    super(message);
  }

  public AdminApiException(String message, Throwable cause) {

    super(message, cause);
  }
}
