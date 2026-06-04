package org.tamtamcatworks.auction.model.notification;

import jakarta.persistence.*;
import org.tamtamcatworks.auction.model.BaseEntity;

@Entity
@Table(name = "notifications")
public class Notification extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    @Column(nullable = false)
    private String message;

    @Column(name = "is_read", nullable = false)
    private boolean isRead = false;

    protected Notification() {}

    public Notification(String userId, NotificationType type, String message) {
        this.userId  = userId;
        this.type    = type;
        this.message = message;
    }

    public void markRead() { this.isRead = true; }

    public String getUserId()          { return userId; }
    public NotificationType getType()  { return type; }
    public String getMessage()         { return message; }
    public boolean isRead()            { return isRead; }
}
