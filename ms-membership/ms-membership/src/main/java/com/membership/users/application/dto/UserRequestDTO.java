package com.membership.users.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO pour la création d'un utilisateur.
 * Best practices :
 * - Séparation des DTOs Request/Response
 * - Validation au niveau DTO
 * - Builder pattern
 * - Pas d'exposition de l'entité directement dans l'API
 */
public class UserRequestDTO {

    @NotBlank(message = "Le prénom ne peut pas être vide")
    @Size(min = 2, max = 50, message = "Le prénom doit contenir entre 2 et 50 caractères")
    private String firstName;

    @NotBlank(message = "Le nom ne peut pas être vide")
    @Size(min = 2, max = 50, message = "Le nom doit contenir entre 2 et 50 caractères")
    private String lastName;

    @NotBlank(message = "L'email ne peut pas être vide")
    @Email(message = "L'email doit être valide")
    private String email;

    // Constructors
    public UserRequestDTO() {
    }

    public UserRequestDTO(String firstName, String lastName, String email) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
    }

    // Getters
    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    // Setters
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    // Builder
    public static UserRequestDTOBuilder builder() {
        return new UserRequestDTOBuilder();
    }

    public static class UserRequestDTOBuilder {
        private String firstName;
        private String lastName;
        private String email;

        public UserRequestDTOBuilder firstName(String firstName) {
            this.firstName = firstName;
            return this;
        }

        public UserRequestDTOBuilder lastName(String lastName) {
            this.lastName = lastName;
            return this;
        }

        public UserRequestDTOBuilder email(String email) {
            this.email = email;
            return this;
        }

        public UserRequestDTO build() {
            return new UserRequestDTO(firstName, lastName, email);
        }
    }
}
