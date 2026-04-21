package com.agribind.communication.dto;

import com.agribind.communication.model.*;
import jakarta.validation.constraints.*;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDateTime;
import java.util.List;

public class MessageTemplateDto {
    private Long id;

    @NotBlank
    private String name;

    @NotBlank
    private String content;

    private List<String> variables;

    // Default constructor
    public MessageTemplateDto() {}

    // All arguments constructor
    public MessageTemplateDto(Long id, String name, String content, List<String> variables) {
        this.id = id;
        this.name = name;
        this.content = content;
        this.variables = variables;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public List<String> getVariables() { return variables; }
    public void setVariables(List<String> variables) { this.variables = variables; }

    // Builder class
    public static class Builder {
        private Long id;
        private String name;
        private String content;
        private List<String> variables;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder name(String name) { this.name = name; return this; }
        public Builder content(String content) { this.content = content; return this; }
        public Builder variables(List<String> variables) { this.variables = variables; return this; }

        public MessageTemplateDto build() {
            return new MessageTemplateDto(id, name, content, variables);
        }
    }

    public static Builder builder() {
        return new Builder();
    }
}