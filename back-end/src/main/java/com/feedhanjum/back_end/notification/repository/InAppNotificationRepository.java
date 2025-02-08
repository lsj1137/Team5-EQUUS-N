package com.feedhanjum.back_end.notification.repository;

import com.feedhanjum.back_end.notification.domain.InAppNotification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InAppNotificationRepository extends JpaRepository<InAppNotification, Long> {
    void removeAllByReceiverIdAndTypeAndIdLessThanEqual(Long receiverId,String type, Long id);
}
