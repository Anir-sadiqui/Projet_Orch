package com.membership.users.application.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

/**
 * DTO pour la réponse d'un utilisateur.
 * Best practices :
 * - Séparation Request/Response
 * - Formatage JSON cohérent pour les dates
 * - Exposition sélective des champs (pas de données sensibles)
 */
public class UserResponseDTO {

    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private Boolean active;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    // Constructors
    public UserResponseDTO() {
    }

    public UserResponseDTO(Long id, String firstName, String lastName, String email, Boolean active,
                          LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.active = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters
    public Long getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public Boolean getActive() {
        return active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    // Setters
    public void setId(Long id) {
        this.id = id;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Builder
    public static UserResponseDTOBuilder builder() {
        return new UserResponseDTOBuilder();
    }

    public static class UserResponseDTOBuilder {
        private Long id;
        private String firstName;
        private String lastName;
        private String email;
        private Boolean active;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public UserResponseDTOBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public UserResponseDTOBuilder firstName(String firstName) {
            this.firstName = firstName;
            return this;
        }

        public UserResponseDTOBuilder lastName(String lastName) {
            this.lastName = lastName;
            return this;
        }

        public UserResponseDTOBuilder email(String email) {
            this.email = email;
            return this;
        }

        public UserResponseDTOBuilder active(Boolean active) {
            this.active = active;
            return this;
        }

        public UserResponseDTOBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public UserResponseDTOBuilder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public UserResponseDTO build() {
            return new UserResponseDTO(id, firstName, lastName, email, active, createdAt, updatedAt);
        }
    }
}
