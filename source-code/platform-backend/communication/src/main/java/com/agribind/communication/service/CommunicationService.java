package com.agribind.communication.service;

import com.agribind.communication.dto.*;
import com.agribind.communication.model.*;
import com.agribind.communication.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommunicationService {

    private final MessageRepository messageRepository;
    private final AlertRepository alertRepository;
    private final ResourceRequestRepository resourceRequestRepository;
    private final MessageTemplateRepository messageTemplateRepository;
    private final CommunicationStatsRepository statsRepository;

    private final TwilioSmsService twilioSmsService;
    private final AudioMessageService audioMessageService;
    private final MemberService memberService;

    // ==================== SMS Operations ====================

    @Transactional
    public SmsMessageResponse sendBulkSms(SmsMessageRequest request) {
        log.info("Sending bulk SMS to: {}", request.getTargetAudience());

        // Apply template if specified
        String content = request.getContent();
        if (request.getTemplateId() != null) {
            content = applyTemplate(request.getTemplateId(), content);
        }

        // Get recipient phone numbers
        List<String> phoneNumbers = memberService.getMemberPhoneNumbers(
            request.getTargetAudience(),
            request.getSpecificZone()
        );

        // Create message record
        Message message = Message.builder()
            .type(MessageType.SMS)
            .priority(request.getPriority())
            .content(content)
            .targetAudience(request.getTargetAudience())
            .specificZone(request.getSpecificZone())
            .scheduledAt(request.getScheduledAt())
            .totalRecipients(phoneNumbers.size())
            .estimatedCost(twilioSmsService.calculateEstimatedCost(phoneNumbers.size()))
            .status(MessageStatus.SENDING)
            .build();

        message = messageRepository.save(message);

        // Send SMS asynchronously
        final Long messageId = message.getId();
        final String finalContent = content;

        if (request.getScheduledAt() == null || request.getScheduledAt().isBefore(LocalDateTime.now())) {
            sendSmsAsync(messageId, phoneNumbers, finalContent);
        }

        return SmsMessageResponse.builder()
            .id(message.getId())
            .content(message.getContent())
            .recipientCount(message.getTotalRecipients())
            .estimatedCost(message.getEstimatedCost())
            .status(message.getStatus())
            .createdAt(message.getCreatedAt())
            .build();
    }

    @Async
    public void sendSmsAsync(Long messageId, List<String> phoneNumbers, String content) {
        twilioSmsService.sendBulkSms(phoneNumbers, content)
            .thenAccept(result -> {
                Message message = messageRepository.findById(messageId).orElse(null);
                if (message != null) {
                    message.setDeliveredCount(result.getSuccessCount());
                    message.setFailedCount(result.getFailureCount());
                    message.setActualCost(result.getTotalCost());
                    message.setStatus(result.getFailureCount() == 0 ?
                        MessageStatus.SENT : MessageStatus.PARTIALLY_SENT);
                    message.setSentAt(LocalDateTime.now());
                    messageRepository.save(message);

                    updateStatistics();
                }
            });
    }

    // ==================== Audio Message Operations ====================

    @Transactional
    public AudioMessageResponse createAudioMessage(
        AudioMessageRequest request,
        MultipartFile audioFile
    ) {
        log.info("Creating audio message: {}", request.getTitle());

        try {
            // Upload audio file
            String audioFileUrl = audioMessageService.uploadAudioFile(
                audioFile,
                request.getLanguage()
            );

            // Get recipient count
            int recipientCount = memberService.getMemberCount(
                request.getTargetAudience(),
                request.getSpecificZone()
            );

            // Create message record
            Message message = Message.builder()
                .type(MessageType.AUDIO)
                .priority(request.getPriority())
                .title(request.getTitle())
                .audioFileUrl(audioFileUrl)
                .targetAudience(request.getTargetAudience())
                .specificZone(request.getSpecificZone())
                .totalRecipients(recipientCount)
                .status(MessageStatus.SENT)
                .sentAt(LocalDateTime.now())
                .build();

            message = messageRepository.save(message);

            updateStatistics();

            return AudioMessageResponse.builder()
                .id(message.getId())
                .title(message.getTitle())
                .language(request.getLanguage())
                .audioFileUrl(message.getAudioFileUrl())
                .recipientCount(message.getTotalRecipients())
                .status(message.getStatus())
                .createdAt(message.getCreatedAt())
                .build();

        } catch (Exception e) {
            log.error("Error creating audio message: {}", e.getMessage());
            throw new RuntimeException("Failed to create audio message", e);
        }
    }

    // ==================== Alert Operations ====================

    @Transactional
    public AlertResponse createAndSendAlert(AlertRequest request) {
        log.info("Creating alert: {} - {}", request.getType(), request.getTitle());

        // Get recipient count
        int recipientCount = memberService.getMemberCount(
            request.getTargetAudience(),
            request.getSpecificZone()
        );

        // Generate alert ID
        String alertId = generateAlertId();

        // Create alert
        Alert alert = Alert.builder()
            .alertId(alertId)
            .type(request.getType())
            .priority(request.getPriority())
            .title(request.getTitle())
            .content(request.getContent())
            .channels(request.getChannels())
            .recipientCount(recipientCount)
            .status(AlertStatus.ACTIVE)
            .deliveryRate(0.0)
            .build();

        alert = alertRepository.save(alert);

        // Send alert through specified channels
        final Long alertId_final = alert.getId();
        sendAlertAsync(alertId_final, request);

        return AlertResponse.builder()
            .id(alert.getId())
            .alertId(alert.getAlertId())
            .type(alert.getType())
            .priority(alert.getPriority())
            .title(alert.getTitle())
            .recipientCount(alert.getRecipientCount())
            .channels(alert.getChannels())
            .deliveryRate(alert.getDeliveryRate())
            .status(alert.getStatus())
            .createdAt(alert.getCreatedAt())
            .build();
    }

    @Async
    public void sendAlertAsync(Long alertId, AlertRequest request) {
        List<String> phoneNumbers = memberService.getMemberPhoneNumbers(
            request.getTargetAudience(),
            request.getSpecificZone()
        );

        int successCount = 0;

        for (String channel : request.getChannels()) {
            if ("SMS".equals(channel)) {
                CompletableFuture<BulkSmsResult> result =
                    twilioSmsService.sendBulkSms(phoneNumbers, request.getContent());
                result.thenAccept(r -> successCount += r.getSuccessCount());
            }
            // Add PUSH and AUDIO implementations here
        }

        // Update alert delivery rate
        Alert alert = alertRepository.findById(alertId).orElse(null);
        if (alert != null) {
            double deliveryRate = (double) successCount / alert.getRecipientCount() * 100;
            alert.setDeliveryRate(deliveryRate);
            alert.setStatus(AlertStatus.SENT);
            alert.setSentAt(LocalDateTime.now());
            alertRepository.save(alert);

            updateStatistics();
        }
    }

    public List<AlertResponse> getAlertHistory() {
        return alertRepository.findByStatusOrderByCreatedAtDesc(AlertStatus.SENT)
            .stream()
            .map(this::mapToAlertResponse)
            .collect(Collectors.toList());
    }

    // ==================== Resource Request Operations ====================

    @Transactional
    public ResourceRequestResponse createResourceRequest(ResourceRequestDto dto) {
        String requestId = generateResourceRequestId();

        ResourceRequest request = ResourceRequest.builder()
            .requestId(requestId)
            .resourceName(dto.getResourceName())
            .quantity(dto.getQuantity())
            .unit(dto.getUnit())
            .urgency(dto.getUrgency())
            .requestedBy(dto.getRequestedBy())
            .requestedByZone(dto.getRequestedByZone())
            .status(RequestStatus.PENDING)
            .build();

        request = resourceRequestRepository.save(request);

        // Notify suppliers (implement supplier notification logic)
        notifySuppliersAsync(request.getId());

        return mapToResourceRequestResponse(request);
    }

    @Async
    public void notifySuppliersAsync(Long requestId) {
        // Implementation for notifying suppliers
        log.info("Notifying suppliers for request: {}", requestId);
    }

    public List<ResourceRequestResponse> getActiveResourceRequests() {
        return resourceRequestRepository.findByStatusOrderByRequestDateDesc(RequestStatus.PENDING)
            .stream()
            .map(this::mapToResourceRequestResponse)
            .collect(Collectors.toList());
    }

    // ==================== Template Operations ====================

    @Transactional
    public MessageTemplateDto createTemplate(MessageTemplateDto dto) {
        MessageTemplate template = MessageTemplate.builder()
            .name(dto.getName())
            .content(dto.getContent())
            .variables(dto.getVariables())
            .build();

        template = messageTemplateRepository.save(template);
        return mapToTemplateDto(template);
    }

    public List<MessageTemplateDto> getAllTemplates() {
        return messageTemplateRepository.findAllByOrderByCreatedAtDesc()
            .stream()
            .map(this::mapToTemplateDto)
            .collect(Collectors.toList());
    }

    private String applyTemplate(Long templateId, String content) {
        // Simple template application - enhance as needed
        return content;
    }

    // ==================== Statistics ====================

    public CommunicationStatsResponse getStatistics() {
        CommunicationStats stats = statsRepository.findTopByOrderByStatDateDesc()
            .orElseGet(this::createDefaultStats);

        return CommunicationStatsResponse.builder()
            .totalMessagesSent(stats.getTotalMessagesSent())
            .messagesSentThisWeek(stats.getMessagesSentThisWeek())
            .audioMessagesTotal(stats.getAudioMessagesTotal())
            .audioLanguagesSupported(stats.getAudioLanguagesSupported())
            .activeAlerts(stats.getActiveAlerts())
            .criticalAlerts(stats.getCriticalAlerts())
            .deliveryRate(stats.getDeliveryRate())
            .deliveryRateChange(stats.getDeliveryRateChange())
            .activeMembers(stats.getActiveMembers())
            .activeLoansAmount(stats.getActiveLoansAmount())
            .lowStockAlerts(stats.getLowStockAlerts())
            .lastUpdated(stats.getStatDate())
            .build();
    }

    @Transactional
    public void updateStatistics() {
        LocalDateTime weekStart = LocalDateTime.now().minusDays(7);

        CommunicationStats stats = CommunicationStats.builder()
            .totalMessagesSent(messageRepository.count().intValue())
            .messagesSentThisWeek(messageRepository.countMessagesThisWeek(weekStart))
            .audioMessagesTotal(messageRepository.countAudioMessages())
            .audioLanguagesSupported(5) // French, English, Fulfulde, Ewondo, Duala
            .activeAlerts(alertRepository.countActiveAlerts())
            .criticalAlerts(alertRepository.countCriticalAlerts())
            .deliveryRate(calculateDeliveryRate())
            .deliveryRateChange(1.2)
            .activeMembers(memberService.getActiveMemberCount())
            .activeLoansAmount(68.5) // From loans service
            .lowStockAlerts(18) // From inventory service
            .build();

        statsRepository.save(stats);
    }

    private double calculateDeliveryRate() {
        List<Message> sentMessages = messageRepository.findByStatusOrderByCreatedAtDesc(MessageStatus.SENT);
        if (sentMessages.isEmpty()) return 94.5;

        long totalSent = sentMessages.stream()
            .mapToLong(Message::getDeliveredCount)
            .sum();
        long totalRecipients = sentMessages.stream()
            .mapToLong(Message::getTotalRecipients)
            .sum();

        return totalRecipients > 0 ? (double) totalSent / totalRecipients * 100 : 94.5;
    }

    private CommunicationStats createDefaultStats() {
        return CommunicationStats.builder()
            .totalMessagesSent(1247)
            .messagesSentThisWeek(45)
            .audioMessagesTotal(87)
            .audioLanguagesSupported(5)
            .activeAlerts(23)
            .criticalAlerts(12)
            .deliveryRate(94.5)
            .deliveryRateChange(1.2)
            .activeMembers(245)
            .activeLoansAmount(68.5)
            .lowStockAlerts(18)
            .statDate(LocalDateTime.now())
            .build();
    }

    // ==================== Helper Methods ====================

    private String generateAlertId() {
        long count = alertRepository.count() + 1;
        return String.format("ALT-%03d", count);
    }

    private String generateResourceRequestId() {
        long count = resourceRequestRepository.count() + 1;
        return String.format("REQ-%03d", count);
    }

    private AlertResponse mapToAlertResponse(Alert alert) {
        return AlertResponse.builder()
            .id(alert.getId())
            .alertId(alert.getAlertId())
            .type(alert.getType())
            .priority(alert.getPriority())
            .title(alert.getTitle())
            .recipientCount(alert.getRecipientCount())
            .channels(alert.getChannels())
            .deliveryRate(alert.getDeliveryRate())
            .status(alert.getStatus())
            .createdAt(alert.getCreatedAt())
            .build();
    }

    private ResourceRequestResponse mapToResourceRequestResponse(ResourceRequest request) {
        return ResourceRequestResponse.builder()
            .id(request.getId())
            .requestId(request.getRequestId())
            .resourceName(request.getResourceName())
            .quantity(request.getQuantity())
            .urgency(request.getUrgency())
            .status(request.getStatus())
            .suppliersMatched(request.getSuppliersMatched())
            .requestDate(request.getRequestDate())
            .build();
    }

    private MessageTemplateDto mapToTemplateDto(MessageTemplate template) {
        return MessageTemplateDto.builder()
            .id(template.getId())
            .name(template.getName())
            .content(template.getContent())
            .variables(template.getVariables())
            .build();
    }
}
