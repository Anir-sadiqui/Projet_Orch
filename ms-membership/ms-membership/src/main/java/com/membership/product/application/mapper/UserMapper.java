package com.membership.product.application.mapper;

import com.membership.product.application.dto.UserRequestDTO;
import com.membership.product.application.dto.UserResponseDTO;
import com.membership.product.domain.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public User toEntity(UserRequestDTO dto) {
        if (dto == null) {
            return null;
        }

        return User.builder()
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .email(dto.getEmail())
                .active(true)
                .build();
    }

    public UserResponseDTO toDto(User user) {
        if (user == null) {
            return null;
        }

        return UserResponseDTO.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .active(user.getActive())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    public void updateEntityFromDto(UserRequestDTO dto, User user) {
        if (dto == null || user == null) {
            return;
        }

        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setEmail(dto.getEmail());
    }
}
