package com.example.coditas.tool.service;

import com.example.coditas.common.dto.PageableDto;
import com.example.coditas.common.enums.UserRole;
import com.example.coditas.common.exception.CustomException;
import com.example.coditas.factory.entity.Factory;
import com.example.coditas.factory.entity.UserFactoryMapping;
import com.example.coditas.tool.dto.*;
import com.example.coditas.tool.entity.*;
import com.example.coditas.tool.enums.RequestNature;
import com.example.coditas.tool.enums.ToolRequestStatus;
import com.example.coditas.tool.repository.*;
import com.example.coditas.user.entity.User;
import com.example.coditas.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ToolRequestService {

    private final ToolRequestRepository toolRequestRepository;
    private final ToolRequestMappingRepository toolRequestMappingRepository;
    private final ToolRepository toolRepository;
    private final ToolService toolService;
    private final UserRepository userRepository;
    private final EntityManager entityManager;

    // ──────────────────────────────────────────────────────────────
    // CREATE TOOL REQUEST (Worker)
    // ──────────────────────────────────────────────────────────────
    @Transactional
    public ToolRequestResponseDto createToolRequest(ToolRequestCreateDto dto) {
        User worker = getCurrentWorker();

        UserFactoryMapping mapping = worker.getFactoryMappings().stream()
                .findFirst()
                .orElseThrow(() -> new CustomException("Worker is not assigned to any factory", HttpStatus.BAD_REQUEST));

        Factory factory = mapping.getFactory();
        String requestNumber = generateRequestNumber();

        ToolRequest request = ToolRequest.builder()
                .requestNumber(requestNumber)
                .factory(factory)
                .worker(worker)
                .nature(RequestNature.FRESH)
                .status(ToolRequestStatus.PENDING)
                .comment(dto.getComment())
                .toolRequestMappings(new ArrayList<>())
                .build();

        ToolRequest savedRequest = toolRequestRepository.saveAndFlush(request);
        entityManager.refresh(savedRequest);

        List<ToolRequestMapping> mappings = new ArrayList<>();
        List<ToolRequestItemDto> items = new ArrayList<>();

        for (ToolRequestItemDto item : dto.getTools()) {
            Tool tool = toolRepository.findByToolId(item.getToolId())
                    .orElseThrow(() -> new CustomException("Tool not found: " + item.getToolId(), HttpStatus.NOT_FOUND));

            ToolRequestMapping toolMapping = ToolRequestMapping.builder()
                    .request(savedRequest)
                    .tool(tool)
                    .quantityRequested(item.getQuantity())
                    .build();

            mappings.add(toolMapping);
            items.add(ToolRequestItemDto.builder()
                    .toolId(tool.getToolId())
                    .toolName(tool.getName())
                    .quantity(item.getQuantity())
                    .build());
        }

        toolRequestMappingRepository.saveAllAndFlush(mappings);
        savedRequest.getToolRequestMappings().addAll(mappings);

        log.info("Tool request created: {} by worker {}", requestNumber, worker.getName());
        return toResponseDto(savedRequest, items);
    }

    // ──────────────────────────────────────────────────────────────
    // APPROVE & ISSUE (Supervisor / Plant Head)
    // ──────────────────────────────────────────────────────────────
    @Transactional
    public List<ToolIssuanceResponseDto> approveAndIssueToolRequest(String requestNumber) {
        ToolRequest request = toolRequestRepository.findByRequestNumber(requestNumber)
                .orElseThrow(() -> new CustomException("Tool Request not found: " + requestNumber, HttpStatus.NOT_FOUND));

        if (request.getStatus() != ToolRequestStatus.PENDING) {
            throw new CustomException("Only PENDING requests can be approved. Current: " + request.getStatus(), HttpStatus.BAD_REQUEST);
        }

        request.setStatus(ToolRequestStatus.APPROVED);
        toolRequestRepository.save(request);

        List<ToolIssuanceResponseDto> issuances = toolService.issueToolsForRequest(request.getRequestNumber());

        User worker = request.getWorker();

        log.info("Tool request APPROVED & ISSUED: {} for worker {}", requestNumber, worker.getName());
        return issuances;
    }

    // ──────────────────────────────────────────────────────────────
    // REJECT (Supervisor / Plant Head)
    // ──────────────────────────────────────────────────────────────
    @Transactional
    public ToolRequestResponseDto rejectToolRequest(String requestNumber, String comment) {
        ToolRequest request = toolRequestRepository.findByRequestNumber(requestNumber)
                .orElseThrow(() -> new CustomException("Tool Request not found: " + requestNumber, HttpStatus.NOT_FOUND));

        if (request.getStatus() != ToolRequestStatus.PENDING) {
            throw new CustomException("Only PENDING requests can be rejected.", HttpStatus.BAD_REQUEST);
        }

        request.setStatus(ToolRequestStatus.REJECTED);
        request.setComment(comment);
        toolRequestRepository.save(request);

        log.info("Tool request RE: {} | Reason: {}", requestNumber, comment);
        return toResponseDto(request, mapItems(request.getToolRequestMappings()));
    }

    // ──────────────────────────────────────────────────────────────
    // GET ALL REQUESTS (Supervisor / Plant Head)
    // ──────────────────────────────────────────────────────────────
    public Page<ToolRequestResponseDto> getAllToolRequests(PageableDto pageReq) {
        User supervisor = getCurrentSupervisor();
        Factory factory = getFactoryForUser(supervisor);

        Pageable pageable = toPageable(pageReq);
        Page<ToolRequest> page = toolRequestRepository.findByFactory(factory, pageable);

        return page.map(r -> toResponseDto(r, mapItems(r.getToolRequestMappings())));
    }

    // ──────────────────────────────────────────────────────────────
    // GET MY REQUESTS (Worker)
    // ──────────────────────────────────────────────────────────────
    public Page<ToolRequestResponseDto> getMyToolRequests(PageableDto pageReq) {
        User worker = getCurrentWorker();
        Pageable pageable = toPageable(pageReq);
        Page<ToolRequest> page = toolRequestRepository.findByWorker(worker, pageable);
        return page.map(r -> toResponseDto(r, mapItems(r.getToolRequestMappings())));
    }

    // ──────────────────────────────────────────────────────────────
    // GET BY REQUEST NUMBER
    // ──────────────────────────────────────────────────────────────
    public ToolRequestResponseDto getToolRequestByNumber(String requestNumber) {
        ToolRequest request = toolRequestRepository.findByRequestNumber(requestNumber)
                .orElseThrow(() -> new CustomException("Request not found: " + requestNumber, HttpStatus.NOT_FOUND));
        return toResponseDto(request, mapItems(request.getToolRequestMappings()));
    }

    // ──────────────────────────────────────────────────────────────
    // HELPER METHODS
    // ──────────────────────────────────────────────────────────────
    private User getCurrentWorker() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException("Worker not found", HttpStatus.UNAUTHORIZED));
    }

    private User getCurrentSupervisor() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException("User not found", HttpStatus.UNAUTHORIZED));
        if (!List.of(UserRole.CHIEF_SUPERVISOR, UserRole.PLANT_HEAD).contains(user.getRole().getName())) {
            throw new CustomException("Access denied", HttpStatus.FORBIDDEN);
        }
        return user;
    }

    private Factory getFactoryForUser(User user) {
        return user.getFactoryMappings().stream()
                .map(UserFactoryMapping::getFactory)
                .findFirst()
                .orElseThrow(() -> new CustomException("User not assigned to factory", HttpStatus.BAD_REQUEST));
    }

    private String generateRequestNumber() {
        long count = toolRequestRepository.count();
        return String.format("TREQ-%06d", count + 1);
    }

    private Pageable toPageable(PageableDto dto) {
        Sort sort = "desc".equalsIgnoreCase(dto.getSortDir())
                ? Sort.by("createdAt").descending()
                : Sort.by("createdAt").ascending();
        return PageRequest.of(dto.getPage(), dto.getSize(), sort);
    }

    private List<ToolRequestItemDto> mapItems(List<ToolRequestMapping> mappings) {
        return mappings.stream().map(m -> {
            Tool t = m.getTool();
            return ToolRequestItemDto.builder()
                    .toolId(t.getToolId())
                    .toolName(t.getName())
                    .quantity(m.getQuantityRequested())
                    .build();
        }).toList();
    }

    private ToolRequestResponseDto toResponseDto(ToolRequest r, List<ToolRequestItemDto> items) {
        return ToolRequestResponseDto.builder()
                .requestNumber(r.getRequestNumber())
                .workerName(r.getWorker().getName())
                .factoryId(r.getFactory().getName())
                .status(r.getStatus().name())
                .comment(r.getComment())
                .requestedOn(r.getCreatedAt())
                .tools(items)
                .build();
    }
}