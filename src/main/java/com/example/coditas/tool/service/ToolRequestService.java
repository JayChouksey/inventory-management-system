package com.example.coditas.tool.service;

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
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    // APPROVE & ISSUE (Supervisor / Plant Head)
    // ──────────────────────────────────────────────────────────────
    @Transactional
    public List<ToolIssuanceResponseDto> approveAndIssueToolRequest(String toolRequestId) {
        ToolRequest request = toolRequestRepository.findByRequestNumber(toolRequestId)
                .orElseThrow(() -> new CustomException("Tool Request not found with ID: " + toolRequestId, HttpStatus.NOT_FOUND));

        if (request.getStatus() != ToolRequestStatus.PENDING) {
            throw new CustomException(
                    "Only PENDING requests can be approved. Current status: " + request.getStatus(),
                    HttpStatus.BAD_REQUEST
            );
        }

        request.setStatus(ToolRequestStatus.APPROVED);
        toolRequestRepository.save(request);

        // Issue tools via ToolService
        List<ToolIssuanceResponseDto> issuances = toolService.issueToolsForRequest(toolRequestId);

        User worker = request.getWorker();

        log.info("Tool request APPROVED & ISSUED: {} for worker {}", request.getRequestNumber(), worker.getName());
        return issuances;
    }

    // ──────────────────────────────────────────────────────────────
    // REJECT (Supervisor / Plant Head)
    // ──────────────────────────────────────────────────────────────
    @Transactional
    public void rejectToolRequest(String toolRequestId, String comment) {
        ToolRequest request = toolRequestRepository.findByRequestNumber(toolRequestId)
                .orElseThrow(() -> new CustomException("Tool Request not found with ID: " + toolRequestId, HttpStatus.NOT_FOUND));

        if (request.getStatus() != ToolRequestStatus.PENDING) {
            throw new CustomException("Only PENDING requests can be rejected.", HttpStatus.BAD_REQUEST);
        }

        request.setStatus(ToolRequestStatus.REJECTED);
        request.setComment(comment);
        toolRequestRepository.save(request);

        log.info("Tool request REJECTED: {} | Reason: {}", request.getRequestNumber(), comment);
    }

    // ──────────────────────────────────────────────────────────────
    // CREATE TOOL REQUEST (Worker)
    // ──────────────────────────────────────────────────────────────
    @Transactional
    public ToolRequestResponseDto createToolRequest(ToolRequestCreateDto dto) {
        User worker = getCurrentWorker();

        UserFactoryMapping userMapping = worker.getFactoryMappings().stream()
                .findFirst()
                .orElseThrow(() -> new CustomException("Worker is not assigned to any factory", HttpStatus.BAD_REQUEST));

        Factory factory = userMapping.getFactory();

        String requestNumber = generateRequestNumber();

        ToolRequest toolRequest = ToolRequest.builder()
                .requestNumber(requestNumber)
                .factory(factory)
                .worker(worker)
                .nature(RequestNature.FRESH)
                .status(ToolRequestStatus.PENDING)
                .comment(dto.getComment())
                .build();

        ToolRequest savedRequest = toolRequestRepository.saveAndFlush(toolRequest);
        entityManager.refresh(savedRequest);

        List<ToolRequestMapping> mappings = new ArrayList<>();
        for (ToolRequestItemDto item : dto.getTools()) {
            Tool tool = toolRepository.findByToolId(item.getToolId())
                    .orElseThrow(() -> new NoSuchElementException("Tool not found with ID: " + item.getToolId()));

            ToolRequestMapping mapping = ToolRequestMapping.builder()
                    .request(savedRequest)
                    .tool(tool)
                    .quantityRequested(item.getQuantity())
                    .build();
            mappings.add(mapping);
        }

        toolRequestMappingRepository.saveAll(mappings);
        savedRequest.setToolRequestMappings(mappings);


        return toToolRequestDto(savedRequest);
    }

    // ──────────────────────────────────────────────────────────────
    // HELPER METHODS
    // ──────────────────────────────────────────────────────────────
    private User getCurrentWorker() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail("shalini.iyer@company.com") // TODO: Change it after JWT implementation
                .orElseThrow(() -> new CustomException("Worker not found", HttpStatus.UNAUTHORIZED));
    }

    private String generateRequestNumber() {
        long time = System.currentTimeMillis();
        return "REQ-" + time;
    }

    private ToolRequestResponseDto toToolRequestDto(ToolRequest request) {
        List<ToolRequestItemDto> items = request.getToolRequestMappings().stream()
                .map(mapping -> {
                    ToolRequestItemDto itemDto = ToolRequestItemDto.builder()
                            .toolId(mapping.getTool().getToolId())
                            .quantity(mapping.getQuantityRequested())
                            .build();

                    return itemDto;
                }).toList();

        return ToolRequestResponseDto.builder()
                .requestId(request.getId())
                .requestNumber(request.getRequestNumber())
                .workerName(request.getWorker().getName())
                .factoryId(request.getFactory().getFactoryId())
                .status(request.getStatus())
                .comment(request.getComment())
                .createdAt(request.getCreatedAt().toLocalDateTime())
                .tools(items)
                .build();
    }
}