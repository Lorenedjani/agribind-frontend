package com.agribind.communication.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// ==================== Message Template Entity ====================
@Entity
@Table(name = "message_templates")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageTemplate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(columnDefinition = "TEXT")
    private String content;

    @ElementCollection
    @CollectionTable(name = "template_variables", joinColumns = @JoinColumn(name = "template_id"))
    @Column(name = "variable")
    private List<String> variables = new ArrayList<>(); // {name}, {amount}, {date}

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
