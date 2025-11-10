package com.example.coditas.factory.service;

import com.example.coditas.user.entity.Role;
import com.example.coditas.user.entity.User;
import com.example.coditas.user.repository.RoleRepository;
import com.example.coditas.user.repository.UserRepository;
import com.example.coditas.centraloffice.entity.CentralOffice;
import com.example.coditas.centraloffice.repository.CentralOfficeRepository;
import com.example.coditas.common.dto.PageableDto;
import com.example.coditas.common.enums.ActiveStatus;
import com.example.coditas.common.enums.UserRole;
import com.example.coditas.common.exception.CustomException;
import com.example.coditas.factory.dto.FactoryCreateRequestDto;
import com.example.coditas.factory.dto.FactoryFilterDto;
import com.example.coditas.factory.dto.FactoryResponseDto;
import com.example.coditas.factory.dto.FactoryUpdateRequestDto;
import com.example.coditas.factory.entity.Factory;
import com.example.coditas.factory.repository.FactoryRepository;
import com.example.coditas.factory.repository.FactorySpecifications;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;

// FactoryService.java
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class FactoryService {

    private final FactoryRepository factoryRepository;
    private final CentralOfficeRepository centralOfficeRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EntityManager entityManager;

    @Transactional
    public FactoryResponseDto createFactory(FactoryCreateRequestDto dto) {

        // Validate Central Office
        CentralOffice centralOffice = centralOfficeRepository.findByCentralOfficeId(dto.getCentralOfficeId())
                .orElseThrow(() -> new CustomException("Central office not found", HttpStatus.NOT_FOUND));

        // Generate factoryId
        String factoryId = generateUniqueFactoryId();

        User plantHead = resolveOrCreatePlantHead(dto);

        // Build Factory
        Factory factory = Factory.builder()
                .factoryId(factoryId)
                .name(dto.getName().trim())
                .city(dto.getCity().trim())
                .address(dto.getAddress())
                .centralOffice(centralOffice)
                .plantHead(plantHead)
                .isActive(ActiveStatus.ACTIVE)
                .build();

        factory = factoryRepository.saveAndFlush(factory);
        entityManager.refresh(factory);

        log.info("Factory created: {} ({}) by admin", factory.getName(), factory.getFactoryId());

        return toDto(factory);
    }

    @Transactional
    public FactoryResponseDto updateFactory(String id, FactoryUpdateRequestDto dto) {
        Factory factory = factoryRepository.findActiveByFactoryId(id)
                .orElseThrow(() -> new CustomException("Factory not found or inactive", HttpStatus.NOT_FOUND));

        if (dto.getName() != null && !dto.getName().trim().isBlank()) {
            factory.setName(dto.getName().trim());
        }
        if (dto.getCity() != null && !dto.getCity().trim().isBlank()) {
            factory.setCity(dto.getCity().trim());
        }
        if (dto.getAddress() != null) {
            factory.setAddress(dto.getAddress());
        }
        if (dto.getCentralOfficeId() != null) {
            CentralOffice co = centralOfficeRepository.findByCentralOfficeId(dto.getCentralOfficeId())
                    .orElseThrow(() -> new CustomException("Central office not found", HttpStatus.NOT_FOUND));
            factory.setCentralOffice(co);
        }
        if (dto.getPlantHeadId() != null) {
            User newHead = userRepository.findActiveByUserId(dto.getPlantHeadId())
                    .orElseThrow(() -> new CustomException("Plant head not found or inactive", HttpStatus.NOT_FOUND));
            if (!newHead.getRole().getName().equals(UserRole.PLANT_HEAD)) {
                throw new CustomException("Selected user is not a Plant Head", HttpStatus.BAD_REQUEST);
            }
            factory.setPlantHead(newHead);
        }

        factory = factoryRepository.save(factory);

        log.info("Factory updated: {} ({})", factory.getName(), factory.getFactoryId());
        return toDto(factory);
    }

    @Transactional
    public String softDeleteFactory(String id) {
        Factory factory = factoryRepository.findByFactoryId(id)
                .orElseThrow(() -> new CustomException("Factory not found", HttpStatus.NOT_FOUND));

        if (factory.getIsActive() == ActiveStatus.INACTIVE) {
            throw new CustomException("Factory already deleted", HttpStatus.CONFLICT);
        }

        factory.setIsActive(ActiveStatus.INACTIVE);
        factoryRepository.save(factory);

        log.info("Factory soft deleted: {} ({})", factory.getName(), factory.getFactoryId());
        return "Factory deleted successfully!";
    }

    public Page<FactoryResponseDto> searchFactories(FactoryFilterDto filter, PageableDto pageReq) {
        Specification<Factory> spec = FactorySpecifications.withFilters(filter);
        Pageable pageable = toPageable(pageReq);
        Page<Factory> page = factoryRepository.findAll(spec, pageable);

        return page.map(this::toDto);
    }

    public Page<FactoryResponseDto> globalSearch(String q, PageableDto pageReq) {
        Specification<Factory> spec = FactorySpecifications.globalSearch(q);
        Pageable pageable = toPageable(pageReq);
        Page<Factory> page = factoryRepository.findAll(spec, pageable);

        return page.map(this::toDto);
    }

    public static Pageable toPageable(PageableDto dto) {
        Sort sort = Sort.by(
                "asc".equalsIgnoreCase(dto.getSortDir()) ? Sort.Direction.ASC : Sort.Direction.DESC,
                dto.getSortBy()
        );
        return PageRequest.of(dto.getPage(), dto.getSize(), sort);
    }

    private User resolveOrCreatePlantHead(FactoryCreateRequestDto dto) {
        if (dto.getPlantHeadId() != null) {
            return userRepository.findActiveByUserId(dto.getPlantHeadId())
                    .orElseThrow(() -> new CustomException("Plant head not found", HttpStatus.NOT_FOUND));
        }

        if (dto.getNewPlantHeadEmail() == null || dto.getNewPlantHeadEmail().isBlank()) {
            throw new CustomException("Plant head email is required", HttpStatus.BAD_REQUEST);
        }

        if (userRepository.existsByEmail(dto.getNewPlantHeadEmail().trim().toLowerCase())) {
            throw new CustomException("Email already exists: " + dto.getNewPlantHeadEmail(), HttpStatus.CONFLICT);
        }

        Role plantHeadRole = roleRepository.findByName(UserRole.PLANT_HEAD)
                .orElseThrow(() -> new CustomException("Plant Head role not found", HttpStatus.NOT_FOUND));

        String tempPassword = "12345678"; // TODO: generate random
        String userId = generateUniqueUserId();

        User newUser = User.builder()
                .userId(userId)
                .name(dto.getNewPlantHeadName().trim())
                .email(dto.getNewPlantHeadEmail().trim().toLowerCase())
                .password(passwordEncoder.encode(tempPassword))
                .phone(dto.getNewPlantHeadPhone())
                .role(plantHeadRole)
                .isActive(ActiveStatus.ACTIVE)
                .build();

        newUser = userRepository.saveAndFlush(newUser);
        entityManager.refresh(newUser);

        // TODO: Send email with credentials
        log.info("New Plant Head created: {} ({})", newUser.getName(), newUser.getEmail());

        return newUser;
    }

    private String generateUniqueFactoryId() {
        long count = factoryRepository.count();
        return String.format("FAC-%03d", count + 1);
    }

    private String generateUniqueUserId() {
        long count = userRepository.count();
        return String.format("USR-%04d", count + 1);
    }

    private FactoryResponseDto toDto(Factory f) {
        String plantHeadName = f.getPlantHead() != null ? f.getPlantHead().getName() : "Not Assigned";

        return FactoryResponseDto.builder()
                .factoryId(f.getFactoryId())
                .name(f.getName())
                .city(f.getCity())
                .address(f.getAddress())
                .plantHeadName(plantHeadName)
                .status(f.getIsActive().name())
                .createdOn(f.getCreatedAt().format(DateTimeFormatter.ofPattern("dd MMM yyyy")))
                .build();
    }
}
