package com.example.coditas.tool.repository;

import com.example.coditas.factory.entity.Factory;
import com.example.coditas.tool.entity.ToolRequest;
import com.example.coditas.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ToolRequestRepository extends JpaRepository<ToolRequest, Long> {

    Optional<ToolRequest> findByRequestNumber(String requestNumber);

    Page<ToolRequest> findByFactory(Factory factory, Pageable pageable);

    Page<ToolRequest> findByWorker(User worker, Pageable pageable);
}
