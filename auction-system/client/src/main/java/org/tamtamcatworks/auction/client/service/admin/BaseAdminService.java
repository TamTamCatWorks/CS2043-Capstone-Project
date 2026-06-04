package org.tamtamcatworks.auction.client.service.admin;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestClient;
import org.tamtamcatworks.auction.client.ApiClient;
import org.tamtamcatworks.auction.client.SessionManager;

public abstract class BaseAdminService {

  protected final ApiClient apiClient = SessionManager.getApiClient();

  protected RestClient client() {

    return apiClient.client();
  }

  protected <T> T get(String uri, Class<T> responseType) {

    return client().get().uri(uri).retrieve().body(responseType);
  }

  protected <T> T get(String uri, ParameterizedTypeReference<T> type) {

    return client().get().uri(uri).retrieve().body(type);
  }

  protected void patch(String uri) {

    client().patch().uri(uri).retrieve().toBodilessEntity();
  }

  protected void delete(String uri) {

    client().delete().uri(uri).retrieve().toBodilessEntity();
  }
}
