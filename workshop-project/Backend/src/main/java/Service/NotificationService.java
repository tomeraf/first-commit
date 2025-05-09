package Service;

import Domain.Repositories.INotificationRepository;
import Domain.DTOs.NotificationDTO;
import com.halilovindustries.websocket.Broadcaster;

import java.time.LocalDateTime;
import java.util.Queue;

public class NotificationService {
    private final INotificationRepository repo;

    public NotificationService(INotificationRepository repo) {
        this.repo = repo;
    }

    /**
     * Create a new notification for the given user, persist it,
     * and immediately push it over WebSocket.
     *
     * @param userId  your internal user ID
     * @param message the text or payload you want to send
     */
    public void notifyUser(int userId, String message) {
        // 1) build a DTO (you might add timestamp, auto‐ID, etc.)
        NotificationDTO dto = new NotificationDTO(repo.getIdToAssign(), userId, message, LocalDateTime.now());

        // 2) store it
        repo.addNotification(userId, dto);

        // 3) broadcast it to any open UIs
        //    Broadcaster is keyed by String UUID; 
        //    here we just use the numeric ID as a string
        Broadcaster.broadcast(Integer.toString(userId), message);
    }

    /**
     * Fetch all pending notifications for that user.
     */
    public Queue<NotificationDTO> getNotifications(int userId) {
        return repo.getUserNotifications(userId);
    }

    /**
     * Remove one notification after the client has seen it (or clicked it).
     */
    public void deleteNotification(int userId, int notificationId) {
        repo.deleteNotification(userId, notificationId);
    }
}