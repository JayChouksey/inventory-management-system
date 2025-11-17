package com.example.coditas.tool.service;

import com.example.coditas.common.dto.GenericFilterDto;
import com.example.coditas.common.dto.PageableDto;
import com.example.coditas.common.enums.UserRole;
import com.example.coditas.common.exception.CustomException;
import com.example.coditas.common.specification.GenericFilterSpecFactory;
import com.example.coditas.common.util.CloudinaryService;
import com.example.coditas.factory.entity.Factory;
import com.example.coditas.factory.entity.UserFactoryMapping;
import com.example.coditas.factory.repository.FactoryRepository;
import com.example.coditas.tool.dto.*;
import com.example.coditas.tool.entity.*;
import com.example.coditas.tool.enums.Perishable;
import com.example.coditas.tool.enums.ToolIssuanceStatus;
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
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ToolService {

    private final ToolRepository toolRepository;
    private final ToolCategoryRepository categoryRepository;
    private final ToolStockRepository toolStockRepository;
    private final FactoryRepository factoryRepository;
    private final ToolRequestRepository toolRequestRepository;
    private final ToolIssuanceRepository toolIssuanceRepository;
    private final ToolReturnRepository toolReturnRepository;
    private final UserRepository userRepository;
    private final CloudinaryService cloudinaryService;
    private final EntityManager entityManager;

    public Page<ToolResponseDto> searchTools(GenericFilterDto filter, PageableDto pageReq) {
        Specification<Tool> spec = GenericFilterSpecFactory.forTool(filter);
        Pageable pageable = toPageableForTool(pageReq);
        Page<Tool> page = toolRepository.findAll(spec, pageable);
        return page.map(this::toDto);
    }

    public Page<ToolResponseDto> globalSearch(String q, PageableDto pageReq) {
        Specification<Tool> spec = GenericFilterSpecFactory.globalSearch(
                new GenericFilterDto(){{setName(q);}},
                "name","category.name"
        );
        Pageable pageable = toPageableForTool(pageReq);
        Page<Tool> page = toolRepository.findAll(spec, pageable);
        return page.map(this::toDto);
    }

    public ToolResponseDto getToolDetail(String id) {
        Tool tool = toolRepository.findByToolId(id)
                .orElseThrow(() -> new CustomException("Tool not found", HttpStatus.NOT_FOUND));
        return toDto(tool);
    }

    @Transactional
    public ToolResponseDto createTool(ToolCreateRequestDto dto) {
        if (toolRepository.existsByNameIgnoreCase(dto.getName().trim())) {
            throw new CustomException("Tool name already exists", HttpStatus.CONFLICT);
        }

        ToolCategory category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new CustomException("Category not found", HttpStatus.NOT_FOUND));

        String imageUrl = cloudinaryService.uploadFile(dto.getImage());
        String toolId = generateUniqueToolId();

        Tool tool = Tool.builder()
                .toolId(toolId)
                .name(dto.getName().trim())
                .category(category)
                .imageUrl(imageUrl)
                .isPerishable(dto.getIsPerishable())
                .isExpensive(dto.getIsExpensive())
                .threshold(dto.getThreshold() != null ? dto.getThreshold() : 0)
                .build();

        tool = toolRepository.saveAndFlush(tool);
        entityManager.refresh(tool);

        log.info("Tool created: {} ({})", tool.getName(), tool.getToolId());
        return toDto(tool);
    }

    @Transactional
    public ToolResponseDto updateTool(String id, ToolUpdateRequestDto dto) {
        Tool tool = toolRepository.findByToolId(id)
                .orElseThrow(() -> new CustomException("Tool not found", HttpStatus.NOT_FOUND));

        if (dto.getName() != null && !dto.getName().trim().isBlank()) {
            if (!dto.getName().trim().equalsIgnoreCase(tool.getName())
                    && toolRepository.existsByNameIgnoreCase(dto.getName().trim())) {
                throw new CustomException("Tool name already exists", HttpStatus.CONFLICT);
            }
            tool.setName(dto.getName().trim());
        }
        if (dto.getCategoryId() != null) {
            ToolCategory cat = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new CustomException("Category not found", HttpStatus.NOT_FOUND));
            tool.setCategory(cat);
        }
        if (dto.getIsPerishable() != null) tool.setIsPerishable(dto.getIsPerishable());
        if (dto.getIsExpensive() != null) tool.setIsExpensive(dto.getIsExpensive());
        if (dto.getThreshold() != null) tool.setThreshold(dto.getThreshold());
        if (dto.getImage() != null && !dto.getImage().isEmpty()) {
            if (tool.getImageUrl() != null) {
                String publicId = cloudinaryService.extractPublicIdFromUrl(tool.getImageUrl());
                cloudinaryService.deleteFile(publicId);
            }
            tool.setImageUrl(cloudinaryService.uploadFile(dto.getImage()));
        }

        tool.setUpdatedAt(ZonedDateTime.now());
        tool = toolRepository.save(tool);

        log.info("Tool updated: {} ({})", tool.getName(), tool.getToolId());
        return toDto(tool);
    }

    @Transactional
    public String deleteTool(String id) {
        Tool tool = toolRepository.findByToolId(id)
                .orElseThrow(() -> new CustomException("Tool not found", HttpStatus.NOT_FOUND));
        toolRepository.delete(tool);
        log.info("Tool deleted: {} ({})", tool.getName(), tool.getToolId());
        return "Tool deleted successfully!";
    }

    // ──────────────────────────────────────────────────────────────
    // NEW: FACTORY STOCK MANAGEMENT
    // ──────────────────────────────────────────────────────────────
    @Transactional
    public ToolStockResponseDto addStockToFactory(String factoryId, AddToolStockDto dto) {
        Factory factory = factoryRepository.findByFactoryId(factoryId)
                .orElseThrow(() -> new CustomException("Factory not found", HttpStatus.NOT_FOUND));

        Tool tool = toolRepository.findByToolId(dto.getToolId())
                .orElseThrow(() -> new CustomException("Tool not found", HttpStatus.NOT_FOUND));

        Optional<ToolStock> existing = toolStockRepository.findByFactoryAndTool(factory, tool);

        if (existing.isPresent()) {
            ToolStock stock = existing.get();
            toolStockRepository.incrementStock(stock.getId(), dto.getQuantity());
        } else {
            toolStockRepository.insertNewStock(factory.getId(), tool.getId(), dto.getQuantity());
        }

        ToolStock updated = toolStockRepository.findByFactoryAndTool(factory, tool)
                .orElseThrow(() -> new CustomException("Stock update failed", HttpStatus.INTERNAL_SERVER_ERROR));

        log.info("Stock added: {} x{} in factory {}", tool.getName(), dto.getQuantity(), factory.getName());
        return toStockDto(updated);
    }

    public Page<ToolStockResponseDto> getToolsByFactory(String factoryId, PageableDto pageReq) {
        Factory factory = factoryRepository.findByFactoryId(factoryId)
                .orElseThrow(() -> new CustomException("Factory not found", HttpStatus.NOT_FOUND));

        Pageable pageable = toPageable(pageReq);
        Page<ToolStock> page = toolStockRepository.findByFactory(factory, pageable);
        return page.map(this::toStockDto);
    }

    // ──────────────────────────────────────────────────────────────
    // WORKER: MY ISSUED TOOLS
    // ──────────────────────────────────────────────────────────────
    public Page<ToolIssuanceResponseDto> getMyIssuedTools(PageableDto pageReq) {
        User worker = getCurrentWorker();
        Pageable pageable = toPageable(pageReq);

        List<ToolIssuanceStatus> active = List.of(
                ToolIssuanceStatus.ISSUED,
                ToolIssuanceStatus.EXTENDED,
                ToolIssuanceStatus.EXTENSION_REQUESTED
        );

        Page<ToolIssuance> page = toolIssuanceRepository.findByWorkerAndStatusIn(worker, active, pageable);
        return page.map(this::toIssuanceDto);
    }

    // ──────────────────────────────────────────────────────────────
    // WORKER: INITIATE RETURN
    // ──────────────────────────────────────────────────────────────
    @Transactional
    public String initiateReturn(Long issuanceId) {
        User worker = getCurrentWorker();
        ToolIssuance issuance = toolIssuanceRepository.findById(issuanceId)
                .orElseThrow(() -> new CustomException("Issuance not found", HttpStatus.NOT_FOUND));

        if (!issuance.getWorker().getId().equals(worker.getId())) {
            throw new CustomException("You can only return your own tools", HttpStatus.FORBIDDEN);
        }

        if (!List.of(ToolIssuanceStatus.ISSUED, ToolIssuanceStatus.EXTENDED).contains(issuance.getStatus())) {
            throw new CustomException("Tool is not in returnable state", HttpStatus.BAD_REQUEST);
        }

        issuance.setStatus(ToolIssuanceStatus.RETURN_PENDING);
        toolIssuanceRepository.save(issuance);

        log.info("Return initiated: Issuance {} by worker {}", issuanceId, worker.getName());
        return "Return request submitted. Supervisor will verify.";
    }

    // ──────────────────────────────────────────────────────────────
    // SUPERVISOR: ISSUE TOOLS (after approval)
    // ──────────────────────────────────────────────────────────────
    @Transactional
    public List<ToolIssuanceResponseDto> issueToolsForRequest(String requestId) {
        User issuer = getCurrentSupervisor();
        ToolRequest request = toolRequestRepository.findByRequestNumber(requestId)
                .orElseThrow(() -> new CustomException("Request not found", HttpStatus.NOT_FOUND));

        if (request.getStatus() != ToolRequestStatus.APPROVED) {
            throw new CustomException("Only APPROVED requests can be issued", HttpStatus.BAD_REQUEST);
        }

        List<ToolIssuance> issuances = new ArrayList<>();

        for (ToolRequestMapping mapping : request.getToolRequestMappings()) {
            Tool tool = mapping.getTool();
            Long qty = mapping.getQuantityRequested();

            ToolStock stock = toolStockRepository.findByFactoryAndTool(request.getFactory(), tool)
                    .orElseThrow(() -> new CustomException("Stock missing for " + tool.getName(), HttpStatus.INTERNAL_SERVER_ERROR));

            if (stock.getAvailableQuantity() < qty) {
                throw new CustomException(
                        "Insufficient stock: " + tool.getName() + " (Avail: " + stock.getAvailableQuantity() + ")",
                        HttpStatus.BAD_REQUEST
                );
            }

            // Deduct stock
            stock.setAvailableQuantity(stock.getAvailableQuantity() - qty);
            stock.setIssuedQuantity(stock.getIssuedQuantity() + qty);
            toolStockRepository.save(stock);

            // Create issuance
            ToolIssuance issuance = ToolIssuance.builder()
                    .factory(request.getFactory())
                    .request(request)
                    .worker(request.getWorker())
                    .tool(tool)
                    .quantity(qty)
                    .issuer(issuer)
                    .status(ToolIssuanceStatus.ISSUED)
                    .returnDate(LocalDateTime.now().plusDays(7))
                    .build();

            issuances.add(issuance);
        }

        request.setStatus(ToolRequestStatus.FULFILLED);
        toolRequestRepository.save(request);

        List<ToolIssuance> saved = toolIssuanceRepository.saveAllAndFlush(issuances);
        entityManager.flush();

        log.info("Tools ISSUED for request {} by {}", requestId, issuer.getName());
        return saved.stream().map(this::toIssuanceDto).toList();
    }

    // ──────────────────────────────────────────────────────────────
    // SUPERVISOR: RETURN TOOL
    // ──────────────────────────────────────────────────────────────
    @Transactional
    public String returnTool(Long issuanceId, Long fitQuantity, Long unfitQuantity) {
        User processor = getCurrentSupervisor();
        ToolIssuance issuance = toolIssuanceRepository.findById(issuanceId)
                .orElseThrow(() -> new CustomException("Issuance not found", HttpStatus.NOT_FOUND));

        if (!List.of(ToolIssuanceStatus.ISSUED, ToolIssuanceStatus.EXTENDED).contains(issuance.getStatus())) {
            throw new CustomException("Tool not in issued state", HttpStatus.BAD_REQUEST);
        }

        Long total = fitQuantity + unfitQuantity;
        if (!total.equals(issuance.getQuantity())) {
            throw new CustomException("Fit + Unfit must equal issued quantity", HttpStatus.BAD_REQUEST);
        }

        ToolStock stock = toolStockRepository.findByFactoryAndTool(issuance.getFactory(), issuance.getTool())
                .orElseThrow(() -> new CustomException("Stock record missing", HttpStatus.INTERNAL_SERVER_ERROR));

        stock.setIssuedQuantity(stock.getIssuedQuantity() - issuance.getQuantity());
        stock.setAvailableQuantity(stock.getAvailableQuantity() + fitQuantity);
        stock.setTotalQuantity(stock.getTotalQuantity() - unfitQuantity);
        toolStockRepository.save(stock);

        ToolReturn toolReturn = ToolReturn.builder()
                .issuance(issuance)
                .fitQuantity(fitQuantity)
                .unfitQuantity(unfitQuantity)
                .updatedBy(processor)
                .returnedAt(ZonedDateTime.now())
                .build();
        toolReturnRepository.save(toolReturn);

        issuance.setStatus(ToolIssuanceStatus.RETURNED);
        issuance.setReturnedAt(LocalDateTime.now());
        toolIssuanceRepository.save(issuance);

        log.info("Tool RETURNED: {} (Fit: {}, Unfit: {})", issuance.getTool().getName(), fitQuantity, unfitQuantity);
        return "Tool returned successfully.";
    }

    // ──────────────────────────────────────────────────────────────
    // SUPERVISOR: CONFISCATE
    // ──────────────────────────────────────────────────────────────
    @Transactional
    public String confiscateTool(Long issuanceId) {
        ToolIssuance issuance = toolIssuanceRepository.findById(issuanceId)
                .orElseThrow(() -> new CustomException("Issuance not found", HttpStatus.NOT_FOUND));

        if (!List.of(ToolIssuanceStatus.ISSUED, ToolIssuanceStatus.EXTENDED).contains(issuance.getStatus())) {
            throw new CustomException("Tool already processed", HttpStatus.BAD_REQUEST);
        }

        if (issuance.getReturnDate().isAfter(LocalDateTime.now())) {
            throw new CustomException("Cannot confiscate non-overdue tool", HttpStatus.BAD_REQUEST);
        }

        returnTool(issuanceId, 0L, issuance.getQuantity());
        issuance.setStatus(ToolIssuanceStatus.CONFISCATED);
        toolIssuanceRepository.save(issuance);

        log.info("Tool CONFISCATED: {} (Overdue)", issuance.getTool().getName());
        return "Tool confiscated due to overdue return.";
    }

    // ──────────────────────────────────────────────────────────────
    // WORKER: REQUEST EXTENSION
    // ──────────────────────────────────────────────────────────────
    @Transactional
    public String requestExtension(Long issuanceId) {
        User worker = getCurrentWorker();
        ToolIssuance issuance = toolIssuanceRepository.findById(issuanceId)
                .orElseThrow(() -> new CustomException("Issuance not found", HttpStatus.NOT_FOUND));

        if (!issuance.getWorker().getId().equals(worker.getId())) {
            throw new CustomException("Not your tool", HttpStatus.FORBIDDEN);
        }

        if (!List.of(ToolIssuanceStatus.ISSUED, ToolIssuanceStatus.EXTENDED).contains(issuance.getStatus())) {
            throw new CustomException("Tool not extendable", HttpStatus.BAD_REQUEST);
        }

        if (issuance.getReturnDate().isBefore(LocalDateTime.now())) {
            throw new CustomException("Cannot extend overdue tool", HttpStatus.BAD_REQUEST);
        }

        issuance.setStatus(ToolIssuanceStatus.EXTENSION_REQUESTED);
        toolIssuanceRepository.save(issuance);

        log.info("Extension requested: Issuance {} by worker {}", issuanceId, worker.getName());
        return "Extension request sent to supervisor.";
    }

    // ──────────────────────────────────────────────────────────────
    // SUPERVISOR: PROCESS EXTENSION
    // ──────────────────────────────────────────────────────────────
    @Transactional
    public ToolIssuanceResponseDto processExtensionRequest(Long issuanceId, ApproveExtensionDto dto) {
        ToolIssuance issuance = toolIssuanceRepository.findById(issuanceId)
                .orElseThrow(() -> new CustomException("Issuance not found", HttpStatus.NOT_FOUND));

        if (issuance.getStatus() != ToolIssuanceStatus.EXTENSION_REQUESTED) {
            throw new CustomException("No pending extension", HttpStatus.BAD_REQUEST);
        }

        if (dto.getApproved()) {
            issuance.setReturnDate(issuance.getReturnDate().plusDays(7));
            issuance.setStatus(ToolIssuanceStatus.EXTENDED);
            log.info("Extension APPROVED: Issuance {}", issuanceId);
        } else {
            issuance.setStatus(ToolIssuanceStatus.ISSUED);
            log.info("Extension REJECTED: Issuance {}", issuanceId);
        }

        toolIssuanceRepository.save(issuance);
        return toIssuanceDto(issuance);
    }

    // ──────────────────────────────────────────────────────────────
    // SUPERVISOR: OVERDUE TOOLS
    // ──────────────────────────────────────────────────────────────
    public Page<ToolIssuanceResponseDto> getOverdueTools(PageableDto pageReq) {
        User supervisor = getCurrentSupervisor();

        Factory factory = supervisor.getFactoryMappings().stream()
                .map(UserFactoryMapping::getFactory)
                .findFirst()
                .orElseThrow(() -> new CustomException("You are not assigned to a factory.", HttpStatus.BAD_REQUEST));

        Pageable pageable = toPageable(pageReq);
        List<ToolIssuanceStatus> active = List.of(ToolIssuanceStatus.ISSUED, ToolIssuanceStatus.EXTENDED);

        Page<ToolIssuance> page = toolIssuanceRepository.findOverdueTools(
                factory, LocalDateTime.now(), active, pageable);

        return page.map(this::toIssuanceDto);
    }

    // ──────────────────────────────────────────────────────────────
    // HELPER METHODS
    // ──────────────────────────────────────────────────────────────
    private User getCurrentWorker() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail("shalini.iyer@company.com") // TODO: Change it after JWT
                .orElseThrow(() -> new CustomException("Worker not found", HttpStatus.UNAUTHORIZED));
    }

    private User getCurrentSupervisor() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail("manoj.kumar@company.com") // TODO: Change it after JWT
                .orElseThrow(() -> new CustomException("User not found", HttpStatus.UNAUTHORIZED));
        if (!List.of(UserRole.CHIEF_SUPERVISOR, UserRole.PLANT_HEAD).contains(user.getRole().getName())) {
            throw new CustomException("Access denied", HttpStatus.FORBIDDEN);
        }
        return user;
    }

    private String generateUniqueToolId() {
        long count = toolRepository.count();
        return String.format("TOOL-%04d", count + 1);
    }

    private Pageable toPageable(PageableDto dto) {
        Sort sort = "desc".equalsIgnoreCase(dto.getSortDir())
                ? Sort.by("id").descending()
                : Sort.by("id").ascending();
        return PageRequest.of(dto.getPage(), dto.getSize(), sort);
    }

    private Pageable toPageableForTool(PageableDto dto) {
        String field = switch (dto.getSortBy().toLowerCase()) {
            case "name" -> "name";
            case "category" -> "category.name";
            case "perishable" -> "isPerishable";
            case "expensive" -> "isExpensive";
            case "threshold" -> "threshold";
            case "created_on", "createdat" -> "createdAt";
            default -> "createdAt";
        };
        Sort sort = "desc".equalsIgnoreCase(dto.getSortDir())
                ? Sort.by(field).descending()
                : Sort.by(field).ascending();
        return PageRequest.of(dto.getPage(), dto.getSize(), sort);
    }

    private ToolResponseDto toDto(Tool t) {
        Integer total = toolStockRepository.sumTotalQuantityByToolId(t.getId());
        Integer available = toolStockRepository.sumAvailableQuantityByToolId(t.getId());
        total = total != null ? total : 0;
        available = available != null ? available : 0;

        String stockStatus = available >= t.getThreshold() ? "IN_STOCK"
                : available > 0 ? "LOW" : "OUT_OF_STOCK";
        if (t.getIsPerishable() == Perishable.PERISHABLE && available <= 5) {
            stockStatus = "CRITICAL";
        }

        return ToolResponseDto.builder()
                .toolId(t.getToolId())
                .name(t.getName())
                .imageUrl(t.getImageUrl())
                .categoryName(t.getCategory().getName())
                .perishable(t.getIsPerishable().name())
                .expensive(t.getIsExpensive().name())
                .threshold(t.getThreshold())
                .totalStock(total)
                .availableStock(available)
                .stockStatus(stockStatus)
                .createdOn(t.getCreatedAt().format(DateTimeFormatter.ofPattern("dd MMM yyyy")))
                .build();
    }

    private ToolStockResponseDto toStockDto(ToolStock s) {
        return ToolStockResponseDto.builder()
                .toolId(s.getTool().getToolId())
                .toolName(s.getTool().getName())
                .factoryId(s.getFactory().getFactoryId())
                .totalQuantity(s.getTotalQuantity())
                .availableQuantity(s.getAvailableQuantity())
                .issuedQuantity(s.getIssuedQuantity())
                .lastUpdatedAt(s.getLastUpdatedAt())
                .build();
    }

    private ToolIssuanceResponseDto toIssuanceDto(ToolIssuance i) {
        return ToolIssuanceResponseDto.builder()
                .issuanceId(i.getId())
                .toolName(i.getTool().getName())
                .workerName(i.getWorker().getName())
                .issuerName(i.getIssuer() != null ? i.getIssuer().getName() : null)
                .status(i.getStatus())
                .issuedAt(i.getIssuedAt())
                .returnDate(i.getReturnDate())
                .build();
    }
}