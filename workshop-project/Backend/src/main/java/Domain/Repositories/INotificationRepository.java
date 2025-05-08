package Domain.Repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import Domain.DTOs.NotificationDTO;

public interface INotificationRepository extends JpaRepository<NotificationDTO, Long>{
    List<NotificationDTO> findByUserId(int userId);
    void deleteByUserId(int userId);
    
}
    
