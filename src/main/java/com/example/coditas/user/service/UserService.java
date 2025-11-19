package com.example.coditas.user.service;

import com.example.coditas.common.util.TempPasswordGenerator;
import com.example.coditas.user.dto.*;
import com.example.coditas.user.entity.Role;
import com.example.coditas.user.entity.User;
import com.example.coditas.user.repository.RoleRepository;
import com.example.coditas.user.repository.UserRepository;
import com.example.coditas.user.repository.UserSpecifications;
import com.example.coditas.common.dto.PageableDto;
import com.example.coditas.common.enums.ActiveStatus;
import com.example.coditas.common.enums.UserRole;
import com.example.coditas.common.exception.CustomException;
import com.example.coditas.common.util.CloudinaryService;
import com.example.coditas.factory.repository.FactoryRepository;
import com.example.coditas.order.enums.DealerOrderStatus;
import com.example.coditas.order.repository.DealerOrderRepository;
import com.example.coditas.product.repository.FactoryProductionRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.*;
import java.time.temporal.TemporalAdjusters;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Transactional(readOnly = true)
@Slf4j
public class UserService {

    private static final String DEFAULT_PROFILE_PIC = "https://res.cloudinary.com/dltuu1hhs/image/upload/v1761553393/cld-sample.jpg";
    private static final String USER_NOT_FOUND_MSG = "Requester user not found";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final FactoryRepository factoryRepository;
    private final FactoryProductionRepository factoryProductionRepository;
    private final DealerOrderRepository dealerOrderRepository;
    private final PasswordEncoder passwordEncoder;
    private final EntityManager entityManager;
    private final CloudinaryService cloudinaryService;

    @Transactional
    public DistributorSignUpResponseDto createDistributor(DistributorSignUpRequestDto dto) {

        // Business validation
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new CustomException("Email already exists: " + dto.getEmail(), HttpStatus.CONFLICT);
        }

        if (dto.getPhone() != null && userRepository.existsByPhone(dto.getPhone())) {
            throw new CustomException("Phone number already registered: " + dto.getPhone(), HttpStatus.CONFLICT);
        }

        // Find DISTRIBUTOR role
        Role distributorRole = roleRepository.findByName(UserRole.DEALER)
                .orElseThrow(() -> new CustomException("Role 'DISTRIBUTOR' not found in database", HttpStatus.NOT_FOUND));

        // Handle image upload
        String imageUrl = DEFAULT_PROFILE_PIC;
        if (dto.getImage() != null && !dto.getImage().isEmpty()) {
            imageUrl = cloudinaryService.uploadFile(dto.getImage());
        }

        // Generate unique userId
        String userId = generateUniqueUserId();

        // Build User entity
        User distributor = User.builder()
                .userId(userId)
                .name(dto.getName().trim())
                .email(dto.getEmail().trim().toLowerCase())
                .password(passwordEncoder.encode(dto.getPassword()))
                .phone(dto.getPhone())
                .imageUrl(imageUrl)
                .role(distributorRole)
                .isActive(ActiveStatus.ACTIVE)
                .build();

        distributor = userRepository.save(distributor);

        log.info("Distributor created successfully: {} ({})", distributor.getName(), distributor.getUserId());

        return DistributorSignUpResponseDto.builder()
                .userId(distributor.getUserId())
                .name(distributor.getName())
                .email(distributor.getEmail())
                .phone(distributor.getPhone())
                .build();
    }

    @Transactional
    public UserResponseDto createUser(UserCreateRequestDto dto){
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User createdBy = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND_MSG, HttpStatus.NOT_FOUND));

        if (createdBy.getRole().getName().equals(UserRole.PLANT_HEAD) && !dto.getRole().equalsIgnoreCase("WORKER") &&
                    !dto.getRole().equalsIgnoreCase("CHIEF_SUPERVISOR")) {
                throw new CustomException("Plant Head can only add Workers and Chief Supervisors", HttpStatus.BAD_REQUEST);
            }


        // Business validation
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new CustomException("Email already exists: " + dto.getEmail(), HttpStatus.CONFLICT);
        }

        if (dto.getPhone() != null && userRepository.existsByPhone(dto.getPhone())) {
            throw new CustomException("Phone number already registered: " + dto.getPhone(), HttpStatus.CONFLICT);
        }

        Role userRole = roleRepository.findByName(UserRole.getRole(dto.getRole().toUpperCase()))
                .orElseThrow(() -> new CustomException("Role not found in database", HttpStatus.NOT_FOUND));

        // Handle image upload
        String imageUrl = DEFAULT_PROFILE_PIC;
        if (dto.getImage() != null && !dto.getImage().isEmpty()) {
            imageUrl = cloudinaryService.uploadFile(dto.getImage());
        }

        // Generate unique userId
        String userId = generateUniqueUserId();
        String password = TempPasswordGenerator.generatePassword(dto.getName(), dto.getPhone());

        // Build User entity
        User newUser = User.builder()
                .userId(userId)
                .name(dto.getName().trim())
                .email(dto.getEmail().trim().toLowerCase())
                .password(passwordEncoder.encode(password))
                .phone(dto.getPhone())
                .imageUrl(imageUrl)
                .role(userRole)
                .createdBy(createdBy)
                .isActive(ActiveStatus.ACTIVE)
                .build();

        // Save with flush to catch DB constraints early
        User savedUser = userRepository.save(newUser);

        entityManager.refresh(newUser); // Ensure fresh state
        log.info("User created successfully: {} ({})", newUser.getName(), newUser.getUserId());

        return toDto(savedUser);
    }

    @Transactional
    public UserResponseDto updateUser(String userId, UserUpdateRequestDto dto) {

        // Find user (must be ACTIVE)
        User user = userRepository.findByUserIdAndIsActive(userId, ActiveStatus.ACTIVE)
                .orElseThrow(() -> new CustomException("User not found or inactive", HttpStatus.NOT_FOUND));


        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND_MSG, HttpStatus.NOT_FOUND));
        boolean isSelf = currentUser.getUserId().equals(userId);

        if (!isSelf) {
            throw new CustomException("You can only update your own profile", HttpStatus.FORBIDDEN);
        }

        // Update name
        if (dto.getName() != null && !dto.getName().trim().isBlank()) {
            user.setName(dto.getName().trim());
        }

        // Update phone (with uniqueness check, except for himself)
        if (dto.getPhone() != null && !dto.getPhone().trim().isBlank()) {
            if (!dto.getPhone().equals(user.getPhone())) {
                if (userRepository.existsByPhone(dto.getPhone())) {
                    throw new CustomException("Phone number already in use", HttpStatus.CONFLICT);
                }
                user.setPhone(dto.getPhone());
            }
        }

        // Update image
        if (dto.getImage() != null && !dto.getImage().isEmpty()) {
            try {
                // Delete old image if not default
                if (user.getImageUrl() != null && !user.getImageUrl().contains(DEFAULT_PROFILE_PIC)) {
                    String publicId = cloudinaryService.extractPublicIdFromUrl(user.getImageUrl());
                    cloudinaryService.deleteFile(publicId);
                }

                String newImageUrl = cloudinaryService.uploadFile(dto.getImage());
                user.setImageUrl(newImageUrl);

            } catch (Exception e) {
                log.warn("Image update failed for user: {}", userId, e);
                throw new CustomException("Failed to update image", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        // Save
        user.setUpdatedAt(ZonedDateTime.now());
        user = userRepository.save(user);

        log.info("User updated: {} by {}", userId, currentUser.getName());

        return toDto(user);
    }

    @Transactional
    public String softDeleteUser(String userId) {

        // Find user
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new CustomException("User not found: " + userId, HttpStatus.NOT_FOUND));

        // Prevent self-delete
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND_MSG, HttpStatus.NOT_FOUND));
        if (currentUser != null && currentUser.getUserId().equals(userId)) {
            throw new CustomException("You cannot delete yourself!", HttpStatus.FORBIDDEN);
        }


        // TODO

        // Prevent deleting admins
        if (user.getRole().getName().equals(UserRole.ADMIN)) {
            throw new CustomException("Cannot delete ADMIN users", HttpStatus.FORBIDDEN);
        }

        // Already inactive check
        if (user.getIsActive() == ActiveStatus.INACTIVE) {
            log.info("User already deleted (soft): {}", userId);
            throw new CustomException("User already deleted", HttpStatus.CONFLICT);
        }

        // Soft delete
        user.setIsActive(ActiveStatus.INACTIVE);

        userRepository.save(user);

        log.info("User soft deleted: {} by {}", userId, currentUser != null ? currentUser.getUserId() : "system");

        return "User delete successfully!";
    }

    // Search with filters + pagination + sorting
    public Page<UserResponseDto> searchUsers(UserFilterDto filter, String query,  PageableDto pageReq) {
        Specification<User> spec = UserSpecifications.withFilters(
                filter.getName(),
                filter.getEmail(),
                filter.getPhone(),
                filter.getRoleId(),
                filter.getFactoryId(),
                filter.getBayId(),
                filter.getStatus()
        );
        if (StringUtils.hasText(query)) spec = spec.and(UserSpecifications.globalSearch(query));

        Pageable pageable = toPageable(pageReq);

        Page<User> page = userRepository.findAll(spec, pageable);

        return page.map(UserService::toDto);
    }

    //  Global text search
    public Page<UserResponseDto> globalSearch(String query, PageableDto pageReq) {
        Specification<User> spec = UserSpecifications.globalSearch(query);

        Pageable pageable = toPageable(pageReq);

        Page<User> page = userRepository.findAll(spec, pageable);

        return page.map(UserService::toDto);
    }

    // PLANT HEAD: Factory Workers
    public Page<UserResponseDto> getFactoryWorkers(String factoryId, PageableDto pageReq) {
        Specification<User> spec = UserSpecifications.factoryWorkers(factoryId);
        Pageable pageable = toPageable(pageReq);

        return userRepository.findAll(spec, pageable)
                .map(UserService::toDto);
    }

    // CHIEF SUPERVISOR: Bay Workers
    public Page<UserResponseDto> getBayWorkers(String bayId, PageableDto pageReq) {
        Specification<User> spec = UserSpecifications.bayWorkers(bayId);
        Pageable pageable = toPageable(pageReq);

        return userRepository.findAll(spec, pageable)
                .map(UserService::toDto);
    }

    // CENTRAL OFFICE: All Dealers (SIMPLEST - BUILT-IN JPA!)
    public Page<UserResponseDto> getAllDealers(PageableDto pageReq) {
        Pageable pageable = toPageable(pageReq);

        // Pure JPA method - no Specification needed!
        return userRepository.findByRoleNameAndIsActive(UserRole.DEALER, ActiveStatus.ACTIVE, pageable)
                .map(UserService::toDto);
    }

    // TODO: Check the data, once production data is entered
    // Dashboard data
    @Transactional(readOnly = true)
    public AdminDashboardDto getAdminDashboardData() {
        ZonedDateTime now = ZonedDateTime.now();

        // Month boundaries as LocalDate
        LocalDate startOfMonthLocal = now.withDayOfMonth(1).toLocalDate();
        LocalDate endOfMonthLocal = now.with(TemporalAdjusters.lastDayOfMonth()).toLocalDate();

        // Convert to LocalDateTime for DealerOrder (timestamp with time)
        ZonedDateTime startOfMonth = now
                .withDayOfMonth(1)
                .toLocalDate()
                .atStartOfDay(now.getZone());

        ZonedDateTime endOfMonth = now
                .with(TemporalAdjusters.lastDayOfMonth())
                .with(LocalTime.MAX);


        // Counts
        long totalFactories = factoryRepository.countByIsActive(ActiveStatus.ACTIVE);
        long totalEmployees = userRepository.countEmployeesByRolesAndActive(
                List.of(UserRole.PLANT_HEAD, UserRole.CHIEF_SUPERVISOR,
                        UserRole.WORKER, UserRole.CENTRAL_OFFICE_HEAD)
        );
        long totalDealers = userRepository.countByRole_NameAndIsActive(UserRole.DEALER, ActiveStatus.ACTIVE);
        long totalCustomers = userRepository.countByRole_NameAndIsActive(UserRole.CUSTOMER, ActiveStatus.ACTIVE);

        // Monthly Sales → Only count DELIVERED orders!
        BigDecimal monthlySales = dealerOrderRepository.sumTotalPriceByDateRangeAndStatus(
                startOfMonth,
                endOfMonth,
                DealerOrderStatus.APPROVED
        ).orElse(BigDecimal.ZERO);

        // Production per Factory
        List<FactoryProductionSummaryDto> factoryProductions = factoryRepository
                .findAllByIsActive(ActiveStatus.ACTIVE)
                .stream()
                .map(factory -> {
                    Long totalProduced = factoryProductionRepository
                            .sumQuantityByFactoryAndDateRange(
                                    factory.getId(),
                                    startOfMonthLocal,  // ← LocalDate
                                    endOfMonthLocal     // ← LocalDate
                            );

                    String plantHeadName = factory.getPlantHead() != null && factory.getPlantHead().getName() != null
                            ? factory.getPlantHead().getName()
                            : "Not Assigned";

                    return FactoryProductionSummaryDto.builder()
                            .factoryId(factory.getId())
                            .factoryName(factory.getName())
                            .city(factory.getCity())
                            .plantHeadName(plantHeadName)
                            .totalProducedThisMonth(totalProduced != null ? totalProduced : 0L)
                            .build();
                })
                .sorted(Comparator.comparing(FactoryProductionSummaryDto::getTotalProducedThisMonth).reversed())
                .collect(Collectors.toList());

        return AdminDashboardDto.builder()
                .totalFactories(totalFactories)
                .totalEmployees(totalEmployees)
                .totalDealers(totalDealers)
                .totalCustomers(totalCustomers)
                .monthlySales(monthlySales)
                .salesMonth(YearMonth.from(startOfMonthLocal))
                .factoryProductions(factoryProductions)
                .build();
    }


    // HELPER METHODS

    // TODO: Change it so no repo call will be there
    // Generate sequential userId
    private String generateUniqueUserId() {
        long count = userRepository.count();
        return String.format("USR-%04d", count + 1);
    }

    //Convert entity to DTO
    private static UserResponseDto toDto(User u) {
        List<String> factory = u.getFactoryMappings() == null ? List.of() :
                u.getFactoryMappings().stream()
                        .map(m -> m.getFactory() != null ? m.getFactory().getName() : null)
                        .filter(Objects::nonNull)
                        .distinct()
                        .toList();

        // single bay
        String bay = u.getFactoryMappings() == null ? null :
                u.getFactoryMappings().stream()
                        .map(m -> m.getBay() != null ? m.getBay().getName() : null)
                        .filter(Objects::nonNull)
                        .findFirst()
                        .orElse(null);

        return UserResponseDto.builder()
                .userId(u.getUserId())
                .name(u.getName())
                .email(u.getEmail())
                .phone(u.getPhone())
                .imageUrl(u.getImageUrl())
                .roleName(u.getRole() != null ? u.getRole().getName().getVal() : null)
                .factory(factory)
                .bay(bay)
                .isActive(String.valueOf(u.getIsActive()))
                .build();
    }


    private Pageable toPageable(PageableDto dto) {
        String sortField = "user_id".equalsIgnoreCase(dto.getSortBy())
                ? "userId"
                : dto.getSortBy();

        Sort sort = "desc".equalsIgnoreCase(dto.getSortDir())
                ? Sort.by(sortField).descending()
                : Sort.by(sortField).ascending();

        return PageRequest.of(dto.getPage(), dto.getSize(), sort);
    }



}
