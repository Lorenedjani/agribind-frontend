package com.agribind.communication.service;

import com.agribind.communication.dto.*;
import com.agribind.communication.model.*;
import com.agribind.communication.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CommunicationService {

    private static final Logger log = LoggerFactory.getLogger(CommunicationService.class);

    private final MessageRepository messageRepository;
    private final AlertRepository alertRepository;
    private final ResourceRequestRepository resourceRequestRepository;
    private final MessageTemplateRepository messageTemplateRepository;
    private final CommunicationStatsRepository statsRepository;

    private final TwilioSmsService twilioSmsService;
    private final AudioMessageService audioMessageService;
    private final MemberService memberService;

    public CommunicationService(
            MessageRepository messageRepository,
            AlertRepository alertRepository,
            ResourceRequestRepository resourceRequestRepository,
            MessageTemplateRepository messageTemplateRepository,
            CommunicationStatsRepository statsRepository,
            TwilioSmsService twilioSmsService,
            AudioMessageService audioMessageService,
            MemberService memberService) {
        this.messageRepository = messageRepository;
        this.alertRepository = alertRepository;
        this.resourceRequestRepository = resourceRequestRepository;
        this.messageTemplateRepository = messageTemplateRepository;
        this.statsRepository = statsRepository;
        this.twilioSmsService = twilioSmsService;
        this.audioMessageService = audioMessageService;
        this.memberService = memberService;
    }

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

        // Create message record - MANUAL CREATION
        Message message = new Message();
        message.setType(MessageType.SMS);
        message.setPriority(request.getPriority());
        message.setContent(content);
        message.setTargetAudience(request.getTargetAudience());
        message.setSpecificZone(request.getSpecificZone());
        message.setScheduledAt(request.getScheduledAt());
        message.setTotalRecipients(phoneNumbers.size());
        message.setEstimatedCost(twilioSmsService.calculateEstimatedCost(phoneNumbers.size()));
        message.setStatus(MessageStatus.SENDING);

        message = messageRepository.save(message);

        // Send SMS asynchronously
        final Long messageId = message.getId();
        final String finalContent = content;

        if (request.getScheduledAt() == null || request.getScheduledAt().isBefore(LocalDateTime.now())) {
            sendSmsAsync(messageId, phoneNumbers, finalContent);
        }

        // Manual response creation
        SmsMessageResponse response = new SmsMessageResponse();
        response.setId(message.getId());
        response.setContent(message.getContent());
        response.setRecipientCount(message.getTotalRecipients());
        response.setEstimatedCost(message.getEstimatedCost());
        response.setStatus(message.getStatus());
        response.setCreatedAt(message.getCreatedAt());
        return response;
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

            // Create message record - MANUAL CREATION
            Message message = new Message();
            message.setType(MessageType.AUDIO);
            message.setPriority(request.getPriority());
            message.setTitle(request.getTitle());
            message.setAudioFileUrl(audioFileUrl);
            message.setTargetAudience(request.getTargetAudience());
            message.setSpecificZone(request.getSpecificZone());
            message.setTotalRecipients(recipientCount);
            message.setStatus(MessageStatus.SENT);
            message.setSentAt(LocalDateTime.now());

            message = messageRepository.save(message);

            updateStatistics();

            // Manual response creation
            AudioMessageResponse response = new AudioMessageResponse();
            response.setId(message.getId());
            response.setTitle(message.getTitle());
            response.setLanguage(request.getLanguage());
            response.setAudioFileUrl(message.getAudioFileUrl());
            response.setRecipientCount(message.getTotalRecipients());
            response.setStatus(message.getStatus());
            response.setCreatedAt(message.getCreatedAt());
            return response;

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

        // Create alert - MANUAL CREATION
        Alert alert = new Alert();
        alert.setAlertId(alertId);
        alert.setType(request.getType());
        alert.setPriority(request.getPriority());
        alert.setTitle(request.getTitle());
        alert.setContent(request.getContent());
        alert.setChannels(request.getChannels());
        alert.setRecipientCount(recipientCount);
        alert.setStatus(AlertStatus.ACTIVE);
        alert.setDeliveryRate(0.0);

        alert = alertRepository.save(alert);

        // Send alert through specified channels
        final Long alertIdFinal = alert.getId();
        sendAlertAsync(alertIdFinal, request);

        // Manual response creation
        AlertResponse response = new AlertResponse();
        response.setId(alert.getId());
        response.setAlertId(alert.getAlertId());
        response.setType(alert.getType());
        response.setPriority(alert.getPriority());
        response.setTitle(alert.getTitle());
        response.setRecipientCount(alert.getRecipientCount());
        response.setChannels(alert.getChannels());
        response.setDeliveryRate(alert.getDeliveryRate());
        response.setStatus(alert.getStatus());
        response.setCreatedAt(alert.getCreatedAt());
        return response;
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
                twilioSmsService.sendBulkSms(phoneNumbers, request.getContent())
                    .thenAccept(result -> {
                        // Update with result
                    });
            }
            // Add PUSH and AUDIO implementations here
        }

        // Update alert delivery rate
        Alert alert = alertRepository.findById(alertId).orElse(null);
        if (alert != null) {
            double deliveryRate = phoneNumbers.isEmpty() ? 0.0 :
                (double) successCount / alert.getRecipientCount() * 100;
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

        // Manual creation
        ResourceRequest request = new ResourceRequest();
        request.setRequestId(requestId);
        request.setResourceName(dto.getResourceName());
        request.setQuantity(dto.getQuantity());
        request.setUnit(dto.getUnit());
        request.setUrgency(dto.getUrgency());
        request.setRequestedBy(dto.getRequestedBy());
        request.setRequestedByZone(dto.getRequestedByZone());
        request.setStatus(RequestStatus.PENDING);

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
        // Manual creation
        MessageTemplate template = new MessageTemplate();
        template.setName(dto.getName());
        template.setContent(dto.getContent());
        template.setVariables(dto.getVariables());

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

        // Manual response creation
        CommunicationStatsResponse response = new CommunicationStatsResponse();
        response.setTotalMessagesSent(stats.getTotalMessagesSent());
        response.setMessagesSentThisWeek(stats.getMessagesSentThisWeek());
        response.setAudioMessagesTotal(stats.getAudioMessagesTotal());
        response.setAudioLanguagesSupported(stats.getAudioLanguagesSupported());
        response.setActiveAlerts(stats.getActiveAlerts());
        response.setCriticalAlerts(stats.getCriticalAlerts());
        response.setDeliveryRate(stats.getDeliveryRate());
        response.setDeliveryRateChange(stats.getDeliveryRateChange());
        response.setActiveMembers(stats.getActiveMembers());
        response.setActiveLoansAmount(stats.getActiveLoansAmount());
        response.setLowStockAlerts(stats.getLowStockAlerts());
        response.setLastUpdated(stats.getStatDate());
        return response;
    }

    @Transactional
    public void updateStatistics() {
        LocalDateTime weekStart = LocalDateTime.now().minusDays(7);

        // Manual stats creation
        CommunicationStats stats = new CommunicationStats();
        stats.setTotalMessagesSent((int) messageRepository.count());
        stats.setMessagesSentThisWeek(messageRepository.countMessagesThisWeek(weekStart));
        stats.setAudioMessagesTotal(messageRepository.countAudioMessages());
        stats.setAudioLanguagesSupported(5); // French, English, Fulfulde, Ewondo, Duala
        stats.setActiveAlerts(alertRepository.countActiveAlerts());
        stats.setCriticalAlerts(alertRepository.countCriticalAlerts());
        stats.setDeliveryRate(calculateDeliveryRate());
        stats.setDeliveryRateChange(1.2);
        stats.setActiveMembers(memberService.getActiveMemberCount());
        stats.setActiveLoansAmount(68.5); // From loans service
        stats.setLowStockAlerts(18); // From inventory service
        stats.setStatDate(LocalDateTime.now());

        statsRepository.save(stats);
    }

    private double calculateDeliveryRate() {
        List<Message> sentMessages = messageRepository.findByStatusOrderByCreatedAtDesc(MessageStatus.SENT);
        if (sentMessages.isEmpty()) return 94.5;

        long totalSent = sentMessages.stream()
            .mapToInt(Message::getDeliveredCount)
            .sum();
        long totalRecipients = sentMessages.stream()
            .mapToInt(Message::getTotalRecipients)
            .sum();

        return totalRecipients > 0 ? (double) totalSent / totalRecipients * 100 : 94.5;
    }

    private CommunicationStats createDefaultStats() {
        // Manual default stats creation
        CommunicationStats stats = new CommunicationStats();
        stats.setTotalMessagesSent(1247);
        stats.setMessagesSentThisWeek(45);
        stats.setAudioMessagesTotal(87);
        stats.setAudioLanguagesSupported(5);
        stats.setActiveAlerts(23);
        stats.setCriticalAlerts(12);
        stats.setDeliveryRate(94.5);
        stats.setDeliveryRateChange(1.2);
        stats.setActiveMembers(245);
        stats.setActiveLoansAmount(68.5);
        stats.setLowStockAlerts(18);
        stats.setStatDate(LocalDateTime.now());
        return stats;
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
        // Manual mapping
        AlertResponse response = new AlertResponse();
        response.setId(alert.getId());
        response.setAlertId(alert.getAlertId());
        response.setType(alert.getType());
        response.setPriority(alert.getPriority());
        response.setTitle(alert.getTitle());
        response.setRecipientCount(alert.getRecipientCount());
        response.setChannels(alert.getChannels());
        response.setDeliveryRate(alert.getDeliveryRate());
        response.setStatus(alert.getStatus());
        response.setCreatedAt(alert.getCreatedAt());
        return response;
    }

    private ResourceRequestResponse mapToResourceRequestResponse(ResourceRequest request) {
        // Manual mapping
        ResourceRequestResponse response = new ResourceRequestResponse();
        response.setId(request.getId());
        response.setRequestId(request.getRequestId());
        response.setResourceName(request.getResourceName());
        response.setQuantity(request.getQuantity());
        response.setUrgency(request.getUrgency());
        response.setStatus(request.getStatus());
        response.setSuppliersMatched(request.getSuppliersMatched());
        response.setRequestDate(request.getRequestDate());
        return response;
    }

    private MessageTemplateDto mapToTemplateDto(MessageTemplate template) {
        // Manual mapping
        MessageTemplateDto dto = new MessageTemplateDto();
        dto.setId(template.getId());
        dto.setName(template.getName());
        dto.setContent(template.getContent());
        dto.setVariables(template.getVariables());
        return dto;
    }
}