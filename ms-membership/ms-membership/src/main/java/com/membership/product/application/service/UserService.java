package com.membership.product.application.service;

import com.membership.product.application.dto.UserRequestDTO;
import com.membership.product.application.dto.UserResponseDTO;
import com.membership.product.application.mapper.UserMapper;
import com.membership.product.domain.entity.User;
import com.membership.product.domain.repository.UserRepository;
import com.membership.product.infrastructure.exception.ResourceAlreadyExistsException;
import com.membership.product.infrastructure.exception.ResourceNotFoundException;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    private final Counter usersCreated;
    private final Counter usersUpdated;
    private final Counter usersDeleted;

    public UserService(UserRepository userRepository,
                       UserMapper userMapper,
                       MeterRegistry meterRegistry) {

        this.userRepository = userRepository;
        this.userMapper = userMapper;

        this.usersCreated = buildCounter(meterRegistry, "users.created", "Nombre d'utilisateurs créés");
        this.usersUpdated = buildCounter(meterRegistry, "users.updated", "Nombre d'utilisateurs mis à jour");
        this.usersDeleted = buildCounter(meterRegistry, "users.deleted", "Nombre d'utilisateurs supprimés");
    }


    public List<UserResponseDTO> getAllUsers() {
        log.debug("Récupération de tous les utilisateurs");
        return mapToDto(userRepository.findAll());
    }

    public UserResponseDTO getUserById(Long id) {
        log.debug("Récupération utilisateur id={}", id);
        return userMapper.toDto(findUserById(id));
    }

    public List<UserResponseDTO> getActiveUsers() {
        log.debug("Récupération des utilisateurs actifs");
        return mapToDto(userRepository.findByActiveTrue());
    }

    public List<UserResponseDTO> searchUsersByLastName(String lastName) {
        log.debug("Recherche utilisateurs nom={}", lastName);
        return mapToDto(userRepository.searchByLastName(lastName));
    }


    @Transactional
    public UserResponseDTO createUser(UserRequestDTO dto) {
        log.debug("Création utilisateur email={}", dto.getEmail());

        assertEmailNotExists(dto.getEmail());

        User savedUser = userRepository.save(userMapper.toEntity(dto));
        usersCreated.increment();

        log.info("Utilisateur créé id={}, email={}", savedUser.getId(), savedUser.getEmail());
        return userMapper.toDto(savedUser);
    }

    @Transactional
    public UserResponseDTO updateUser(Long id, UserRequestDTO dto) {
        log.debug("Mise à jour utilisateur id={}", id);

        User user = findUserById(id);
        assertEmailUpdatable(user, dto.getEmail());

        userMapper.updateEntityFromDto(dto, user);
        usersUpdated.increment();

        log.info("Utilisateur mis à jour id={}, email={}", user.getId(), user.getEmail());
        return userMapper.toDto(user);
    }

    @Transactional
    public void deleteUser(Long id) {
        log.debug("Suppression utilisateur id={}", id);

        User user = findUserById(id);
        userRepository.delete(user);
        usersDeleted.increment();

        log.info("Utilisateur supprimé id={}, email={}", id, user.getEmail());
    }

    @Transactional
    public UserResponseDTO deactivateUser(Long id) {
        log.debug("Désactivation utilisateur id={}", id);

        User user = findUserById(id);
        user.setActive(false);

        log.info("Utilisateur désactivé id={}, email={}", id, user.getEmail());
        return userMapper.toDto(user);
    }


    private User findUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
    }

    private void assertEmailNotExists(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new ResourceAlreadyExistsException("User", "email", email);
        }
    }

    private void assertEmailUpdatable(User user, String newEmail) {
        if (!user.getEmail().equals(newEmail) && userRepository.existsByEmail(newEmail)) {
            throw new ResourceAlreadyExistsException("User", "email", newEmail);
        }
    }

    private List<UserResponseDTO> mapToDto(List<User> users) {
        log.info("Nombre d'utilisateurs récupérés: {}", users.size());
        return users.stream().map(userMapper::toDto).toList();
    }

    private Counter buildCounter(MeterRegistry registry, String name, String description) {
        return Counter.builder(name)
                .description(description)
                .tag("type", "user")
                .register(registry);
    }
}
