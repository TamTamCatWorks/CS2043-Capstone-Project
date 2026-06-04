package org.tamtamcatworks.auction.client.service.admin;

import org.tamtamcatworks.auction.client.ApiClient;

public class AdminApiClient {

  private final ApiClient api = new ApiClient();

  public ApiClient api() {
    return api;
  }
}
