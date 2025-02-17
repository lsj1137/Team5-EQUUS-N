package com.feedhanjum.back_end.notification.service;

import com.feedhanjum.back_end.core.domain.JobRecord;
import com.feedhanjum.back_end.core.repository.JobRecordRepository;
import com.feedhanjum.back_end.event.EventPublisher;
import com.feedhanjum.back_end.feedback.domain.Feedback;
import com.feedhanjum.back_end.feedback.event.FeedbackLikedEvent;
import com.feedhanjum.back_end.feedback.event.FeedbackReportCreatedEvent;
import com.feedhanjum.back_end.feedback.event.FrequentFeedbackCreatedEvent;
import com.feedhanjum.back_end.feedback.event.RegularFeedbackCreatedEvent;
import com.feedhanjum.back_end.feedback.repository.FeedbackRepository;
import com.feedhanjum.back_end.member.domain.Member;
import com.feedhanjum.back_end.member.repository.MemberRepository;
import com.feedhanjum.back_end.notification.controller.dto.notification.InAppNotificationDto;
import com.feedhanjum.back_end.notification.domain.*;
import com.feedhanjum.back_end.notification.event.InAppNotificationCreatedEvent;
import com.feedhanjum.back_end.notification.repository.InAppNotificationQueryRepository;
import com.feedhanjum.back_end.notification.repository.InAppNotificationRepository;
import com.feedhanjum.back_end.schedule.domain.Schedule;
import com.feedhanjum.back_end.schedule.event.RegularFeedbackRequestCreatedEvent;
import com.feedhanjum.back_end.schedule.event.ScheduleCreatedEvent;
import com.feedhanjum.back_end.schedule.repository.ScheduleRepository;
import com.feedhanjum.back_end.team.domain.Team;
import com.feedhanjum.back_end.team.domain.TeamMember;
import com.feedhanjum.back_end.team.event.FrequentFeedbackRequestedEvent;
import com.feedhanjum.back_end.team.event.TeamLeaderChangedEvent;
import com.feedhanjum.back_end.team.repository.TeamRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@RequiredArgsConstructor
@Service
public class InAppNotificationService {
    private final InAppNotificationRepository inAppNotificationRepository;
    private final InAppNotificationQueryRepository inAppNotificationQueryRepository;
    private final MemberRepository memberRepository;
    private final ScheduleRepository scheduleRepository;
    private final EventPublisher eventPublisher;
    private final FeedbackRepository feedbackRepository;
    private final TeamRepository teamRepository;
    private final WebPushService webPushService;
    private final JobRecordRepository jobRecordRepository;
    private final Clock clock;


    @Transactional(readOnly = true)
    public List<InAppNotificationDto> getInAppNotifications(Long receiverId) {
        return inAppNotificationQueryRepository.getInAppNotifications(receiverId).stream()
                .map(InAppNotificationDto::from).toList();
    }

    /**
     * @throws EntityNotFoundException receiverId에 해당하는 엔티티가 없을 때
     */
    @Transactional
    public void readInAppNotifications(Long receiverId, List<Long> notificationIds) {
        Member receiver = memberRepository.findById(receiverId).orElseThrow(() -> new EntityNotFoundException("없는 사용자"));
        List<InAppNotification> notifications = inAppNotificationRepository.findAllById(notificationIds);
        for (InAppNotification notification : notifications) {
            notification.read(receiver);
        }
    }

    @Transactional
    public void createNotification(FrequentFeedbackRequestedEvent event) {
        Long senderId = event.senderId();
        Long receiverId = event.receiverId();
        Long teamId = event.teamId();

        Member sender = memberRepository.findById(senderId).orElseThrow();

        InAppNotification notification = new FrequentFeedbackRequestNotification(receiverId, sender.getName(), teamId, senderId);
        inAppNotificationRepository.save(notification);
        eventPublisher.publishEvent(new InAppNotificationCreatedEvent(notification.getId()));
    }

    @Transactional
    public void createNotification(RegularFeedbackRequestCreatedEvent event) {
        Long receiverId = event.receiverId();
        Long scheduleId = event.scheduleId();

        Member receiver = memberRepository.findById(receiverId)
                .orElseThrow(EntityNotFoundException::new);
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(EntityNotFoundException::new);

        InAppNotification notification = new RegularFeedbackRequestNotification(receiver, schedule);
        inAppNotificationRepository.save(notification);
        eventPublisher.publishEvent(new InAppNotificationCreatedEvent(notification.getId()));
    }

    @Transactional
    public void createNotification(FeedbackLikedEvent event) {
        Long feedbackId = event.feedbackId();

        Feedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(EntityNotFoundException::new);

        InAppNotification notification = new HeartReactionNotification(feedback);
        inAppNotificationRepository.save(notification);
        eventPublisher.publishEvent(new InAppNotificationCreatedEvent(notification.getId()));
    }

    @Transactional
    public void createNotification(FrequentFeedbackCreatedEvent event) {
        Long feedbackId = event.feedbackId();

        Feedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(EntityNotFoundException::new);

        InAppNotification notification = new FeedbackReceiveNotification(feedback);
        inAppNotificationRepository.save(notification);
        eventPublisher.publishEvent(new InAppNotificationCreatedEvent(notification.getId()));
    }

    @Transactional
    public void createNotification(RegularFeedbackCreatedEvent event) {
        Long feedbackId = event.feedbackId();

        Feedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(EntityNotFoundException::new);

        InAppNotification notification = new FeedbackReceiveNotification(feedback);
        inAppNotificationRepository.save(notification);
        eventPublisher.publishEvent(new InAppNotificationCreatedEvent(notification.getId()));
    }

    @Transactional
    public void createNotification(FeedbackReportCreatedEvent event) {
        Long receiverId = event.receiverId();
        Long endedTeamId = event.endedTeamId();

        Member receiver = memberRepository.findById(receiverId)
                .orElseThrow(EntityNotFoundException::new);
        Team team = null;
        if (endedTeamId != null)
            team = teamRepository.findById(endedTeamId)
                    .orElseThrow(EntityNotFoundException::new);

        InAppNotification notification = new FeedbackReportCreateNotification(receiver, team);
        inAppNotificationRepository.save(notification);
        eventPublisher.publishEvent(new InAppNotificationCreatedEvent(notification.getId()));
    }

    @Transactional
    public void createNotification(TeamLeaderChangedEvent event) {
        Long teamId = event.teamId();

        Team team = teamRepository.findById(teamId)
                .orElseThrow(EntityNotFoundException::new);


        InAppNotification notification = new TeamLeaderChangeNotification(team);
        inAppNotificationRepository.save(notification);
        eventPublisher.publishEvent(new InAppNotificationCreatedEvent(notification.getId()));
    }

    @Transactional
    public void createNotification(ScheduleCreatedEvent event) {
        Long scheduleId = event.scheduleId();

        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(EntityNotFoundException::new);

        List<TeamMember> teamMembers = schedule.getTeam().getTeamMembers();
        for (TeamMember teamMember : teamMembers) {
            Member receiver = teamMember.getMember();
            InAppNotification notification = new ScheduleCreateNotification(receiver, schedule);
            inAppNotificationRepository.save(notification);
            eventPublisher.publishEvent(new InAppNotificationCreatedEvent(notification.getId()));
        }
    }


    @Transactional
    public void checkUnreadNotifications() {
        JobRecord jobRecord = jobRecordRepository.findById(JobRecord.JobName.UNREAD_NOTIFICATIONS)
                .orElseGet(() -> new JobRecord(JobRecord.JobName.UNREAD_NOTIFICATIONS));
        LocalDateTime previousTime = jobRecord.getPreviousFinishTime();

        LocalDateTime oneDayAgo = LocalDateTime.now(clock).truncatedTo(ChronoUnit.MINUTES).minusDays(1);

        List<FeedbackReceiveNotification> unreadNotifications = inAppNotificationQueryRepository.getUnreadFeedbackReceiveNotifications(previousTime.plusSeconds(1), oneDayAgo);
        unreadNotifications.sort(Comparator.comparing(InAppNotification::getCreatedAt));
        // 같은 사용자를 대상으로 여러개의 안읽은 알림이 있다면 가장 오래된 것에 대해서만 이벤트 발행
        Set<Long> receiverIds = new HashSet<>();
        for (FeedbackReceiveNotification unreadNotification : unreadNotifications) {
            if (receiverIds.contains(unreadNotification.getReceiverId())) {
                continue;
            }
            createNotification(unreadNotification);
            receiverIds.add(unreadNotification.getReceiverId());
        }
        jobRecord.updatePreviousFinishTime(oneDayAgo);
        jobRecordRepository.save(jobRecord);
    }

    private void createNotification(FeedbackReceiveNotification unreadNotification) {
        // 이미 안읽은 미확인 피드백 알림이 있다면 새로 생성하지 않음
        Optional<InAppNotification> exists = inAppNotificationRepository.findByReceiverIdAndType(
                unreadNotification.getReceiverId(),
                NotificationType.UNREAD_FEEDBACK_EXIST);
        if (exists.isPresent() && !exists.get().isRead())
            return;

        InAppNotification notification = new UnreadFeedbackExistNotification(unreadNotification);
        inAppNotificationRepository.save(notification);
        eventPublisher.publishEvent(new InAppNotificationCreatedEvent(notification.getId()));
    }


    @Transactional(readOnly = true)
    public void sendPushNotification(Long notificationId) {
        InAppNotification notification = inAppNotificationRepository.findById(notificationId)
                .orElseThrow(EntityNotFoundException::new);
        Long receiverId = notification.getReceiverId();
        webPushService.sendPushMessage(receiverId, notification);
    }
}
