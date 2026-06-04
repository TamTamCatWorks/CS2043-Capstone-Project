package org.tamtamcatworks.auction.client.service.admin;

import java.util.List;
import org.springframework.core.ParameterizedTypeReference;
import org.tamtamcatworks.auction.shared.response.UserResponse;

public class AdminUserService extends BaseAdminService {

  public List<UserResponse> getUsers() {

    return get("/admin/users", new ParameterizedTypeReference<>() {});
  }

  public void suspendUser(String userId) {

    patch("/admin/users/" + userId + "/suspend");
  }

  public void activateUser(String userId) {

    patch("/admin/users/" + userId + "/activate");
  }
}
