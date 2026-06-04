package org.tamtamcatworks.auction.api.websocket;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.tamtamcatworks.auction.service.event.UserStateEvent;

@Component
public class UserStateBroadcastListener {

  private final SimpMessagingTemplate messagingTemplate;

  public UserStateBroadcastListener(SimpMessagingTemplate messagingTemplate) {
    this.messagingTemplate = messagingTemplate;
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void onUserStateChanged(UserStateEvent event) {
    messagingTemplate.convertAndSend("/topic/user-state/" + event.userId(), event.user());
  }
}
