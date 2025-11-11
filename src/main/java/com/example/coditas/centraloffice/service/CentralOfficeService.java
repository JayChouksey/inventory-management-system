package com.example.coditas.centraloffice.service;

import com.example.coditas.user.entity.Role;
import com.example.coditas.user.entity.User;
import com.example.coditas.user.repository.RoleRepository;
import com.example.coditas.user.repository.UserRepository;
import com.example.coditas.centraloffice.dto.CentralOfficeCreateRequestDto;
import com.example.coditas.centraloffice.dto.CentralOfficeFilterDto;
import com.example.coditas.centraloffice.dto.CentralOfficeResponseDto;
import com.example.coditas.centraloffice.dto.CentralOfficeUpdateRequestDto;
import com.example.coditas.centraloffice.entity.CentralOffice;
import com.example.coditas.centraloffice.repository.CentralOfficeRepository;
import com.example.coditas.centraloffice.repository.CentralOfficeSpecifications;
import com.example.coditas.common.dto.PageableDto;
import com.example.coditas.common.enums.ActiveStatus;
import com.example.coditas.common.enums.UserRole;
import com.example.coditas.common.exception.CustomException;
import com.example.coditas.common.util.CloudinaryService;
import com.example.coditas.order.repository.DealerOrderRepository;
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

@Service
@RequiredArgsConstructor
@Slf4j
public class CentralOfficeService {

    private static final String DEFAULT_PROFILE_PIC = "https://res.cloudinary.com/dltuu1hhs/image/upload/v1761553393/cld-sample.jpg";

    private final CentralOfficeRepository centralOfficeRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final DealerOrderRepository dealerOrderRepository;
    private final CloudinaryService cloudinaryService;
    private final PasswordEncoder passwordEncoder;

    public Page<CentralOfficeResponseDto> searchOffices(CentralOfficeFilterDto filter, PageableDto pageReq) {
        Specification<CentralOffice> spec = CentralOfficeSpecifications.withFilters(filter);
        Pageable pageable = toPageable(pageReq);
        Page<CentralOffice> page = centralOfficeRepository.findAll(spec, pageable);
        return page.map(this::toDto);
    }

    public Page<CentralOfficeResponseDto> globalSearch(String q, PageableDto pageReq) {
        Specification<CentralOffice> spec = CentralOfficeSpecifications.globalSearch(q);
        Pageable pageable = toPageable(pageReq);
        Page<CentralOffice> page = centralOfficeRepository.findAll(spec, pageable);
        return page.map(this::toDto);
    }

    public CentralOfficeResponseDto getOfficeDetail(String id) {
        CentralOffice office = centralOfficeRepository.findByCentralOfficeId(id)
                .orElseThrow(() -> new CustomException("Central Office not found", HttpStatus.NOT_FOUND));
        return toDto(office);
    }

    @Transactional
    public CentralOfficeResponseDto createOffice(CentralOfficeCreateRequestDto dto) {
        String officeId = generateUniqueOfficeId();
        User head = resolveOrCreateHead(dto);

        CentralOffice office = CentralOffice.builder()
                .centralOfficeId(officeId)
                .city(dto.getCity().trim())
                .address(dto.getAddress())
                .head(head)
                .isActive(ActiveStatus.ACTIVE)
                .build();

        office = centralOfficeRepository.save(office);

        log.info("Central Office created: {} ({})", office.getCity(), office.getCentralOfficeId());
        return toDto(office);
    }

    @Transactional
    public CentralOfficeResponseDto updateOffice(String id, CentralOfficeUpdateRequestDto dto) {
        CentralOffice office = centralOfficeRepository.findByCentralOfficeId(id)
                .orElseThrow(() -> new CustomException("Central Office not found", HttpStatus.NOT_FOUND));

        if (dto.getCity() != null && !dto.getCity().trim().isBlank()) {
            office.setCity(dto.getCity().trim());
        }
        if (dto.getAddress() != null) {
            office.setAddress(dto.getAddress());
        }
        if (dto.getHeadId() != null) {
            User newHead = userRepository.findActiveById(dto.getHeadId())
                    .orElseThrow(() -> new CustomException("Head not found", HttpStatus.NOT_FOUND));
            if (!newHead.getRole().getName().equals(UserRole.CENTRAL_OFFICE_HEAD)) {
                throw new CustomException("User is not a Central Office Head", HttpStatus.BAD_REQUEST);
            }
            office.setHead(newHead);
        }

        office = centralOfficeRepository.save(office);

        return toDto(office);
    }

    @Transactional
    public String deleteOffice(String id) {
        CentralOffice office = centralOfficeRepository.findByCentralOfficeId(id)
                .orElseThrow(() -> new CustomException("Central Office not found", HttpStatus.NOT_FOUND));

        long activeCount = centralOfficeRepository.countByIsActive(ActiveStatus.ACTIVE);
        if (activeCount <= 1) {
            throw new CustomException("Cannot delete the last active Central Office", HttpStatus.BAD_REQUEST);
        }

        // Reassign orders
        CentralOffice fallback = centralOfficeRepository
                .findFirstByIsActiveAndCentralOfficeIdNot(ActiveStatus.ACTIVE, id)
                .orElseThrow(() -> new CustomException("No fallback office found", HttpStatus.INTERNAL_SERVER_ERROR));

        dealerOrderRepository.reassignOrders(office.getId(), fallback.getId());
        office.setIsActive(ActiveStatus.INACTIVE);
        centralOfficeRepository.save(office);

        log.info("Central Office deleted: {} → reassigned to {}", office.getCity(), fallback.getCity());
        return "Central Office deleted and orders reassigned successfully!";
    }

    // ──────────────────────────────────────────────────────────────
    private User resolveOrCreateHead(CentralOfficeCreateRequestDto dto) {
        if (dto.getHeadId() != null) {
            return userRepository.findActiveById(dto.getHeadId())
                    .orElseThrow(() -> new CustomException("Head not found", HttpStatus.NOT_FOUND));
        }

        if (dto.getNewHeadEmail() == null || dto.getNewHeadEmail().isBlank()) {
            throw new CustomException("Head email is required", HttpStatus.BAD_REQUEST);
        }
        if (userRepository.existsByEmail(dto.getNewHeadEmail().trim().toLowerCase())) {
            throw new CustomException("Email already exists", HttpStatus.CONFLICT);
        }

        Role role = roleRepository.findByName(UserRole.CENTRAL_OFFICE_HEAD)
                .orElseThrow(() -> new CustomException("Role not found", HttpStatus.NOT_FOUND));

        String imageUrl = DEFAULT_PROFILE_PIC;
        if (dto.getNewHeadImage() != null && !dto.getNewHeadImage().isEmpty()) {
            imageUrl = cloudinaryService.uploadFile(dto.getNewHeadImage());
        }

        String userId = generateUniqueUserId();
        String tempPassword = "12345678"; // TODO: generate

        User user = User.builder()
                .userId(userId)
                .name(dto.getNewHeadName().trim())
                .email(dto.getNewHeadEmail().trim().toLowerCase())
                .password(passwordEncoder.encode(tempPassword))
                .phone(dto.getNewHeadPhone())
                .imageUrl(imageUrl)
                .role(role)
                .isActive(ActiveStatus.ACTIVE)
                .build();

        user = userRepository.save(user);

        log.info("New Central Office Head created: {}", user.getName());
        return user;
    }

    private String generateUniqueOfficeId() {
        long count = centralOfficeRepository.count();
        return String.format("CO-%03d", count + 1);
    }

    private String generateUniqueUserId() {
        long count = userRepository.count();
        return String.format("USR-%04d", count + 1);
    }

    private Pageable toPageable(PageableDto dto) {
        String field = switch (dto.getSortBy().toLowerCase()) {
            case "city", "location" -> "city";
            case "head", "head_name" -> "head.name";
            case "orders", "total_orders" -> "id"; // will sort in service
            case "created_on" -> "createdAt";
            default -> "createdAt";
        };
        Sort sort = "desc".equalsIgnoreCase(dto.getSortDir())
                ? Sort.by(field).descending()
                : Sort.by(field).ascending();
        return PageRequest.of(dto.getPage(), dto.getSize(), sort);
    }

    private CentralOfficeResponseDto toDto(CentralOffice co) {
        Long orders = dealerOrderRepository.countOrdersByCentralOfficeId(co.getId());
        String headName = co.getHead() != null ? co.getHead().getName() : "Not Assigned";
        String headEmail = co.getHead() != null ? co.getHead().getEmail() : null;
        String headImage = co.getHead() != null ? co.getHead().getImageUrl() : null;

        return CentralOfficeResponseDto.builder()
                .centralOfficeId(co.getCentralOfficeId())
                .city(co.getCity())
                .address(co.getAddress())
                .headName(headName)
                .headEmail(headEmail)
                .headImageUrl(headImage)
                .status(co.getIsActive().name())
                .totalOrdersProcessed(orders)
                .createdOn(co.getCreatedAt().format(DateTimeFormatter.ofPattern("dd MMM yyyy")))
                .build();
    }
}