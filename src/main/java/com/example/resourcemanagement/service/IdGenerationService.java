package com.example.resourcemanagement.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.resourcemanagement.repository.AccountRepository;
import com.example.resourcemanagement.repository.ResourceAllocationRepository;
import com.example.resourcemanagement.repository.ResourceRepository;
import com.example.resourcemanagement.repository.ResourceRequestRepository;

@Service
public class IdGenerationService {
    
    @Autowired
    private AccountRepository accountRepository;
    
    @Autowired
    private ResourceRequestRepository resourceRequestRepository;
    
    @Autowired
    private ResourceRepository resourceRepository;
    
    @Autowired
    private ResourceAllocationRepository resourceAllocationRepository;
    
    /**
     * Account ID 생성 (1~9)
     */
    public Long generateAccountId() {
        Long maxId = accountRepository.findAll().stream()
            .map(acc -> acc.getId())
            .max(Long::compareTo)
            .orElse(0L);
        Long nextId = maxId + 1;
        if (nextId > 9) {
            throw new IllegalStateException("Account ID 범위를 초과했습니다. (최대: 9)");
        }
        return nextId;
    }
    
    /**
     * ResourceRequest ID 생성 (10~99)
     */
    public Long generateRequestId() {
        Long maxId = resourceRequestRepository.findAll().stream()
            .map(req -> req.getId())
            .max(Long::compareTo)
            .orElse(9L); // 9는 account의 최대값
        Long nextId = maxId + 1;
        if (nextId < 10) {
            nextId = 10L;
        }
        if (nextId > 99) {
            throw new IllegalStateException("ResourceRequest ID 범위를 초과했습니다. (최대: 99)");
        }
        return nextId;
    }
    
    /**
     * Resource ID 생성 (100~999)
     */
    public Long generateResourceId() {
        Long maxId = resourceRepository.findAll().stream()
            .map(res -> res.getId())
            .max(Long::compareTo)
            .orElse(99L); // 99는 request의 최대값
        Long nextId = maxId + 1;
        if (nextId < 100) {
            nextId = 100L;
        }
        if (nextId > 999) {
            throw new IllegalStateException("Resource ID 범위를 초과했습니다. (최대: 999)");
        }
        return nextId;
    }
    
    /**
     * ResourceAllocation ID 생성 (1000~9999)
     */
    public Long generateAllocationId() {
        Long maxId = resourceAllocationRepository.findAll().stream()
            .map(alloc -> alloc.getId())
            .max(Long::compareTo)
            .orElse(999L); // 999는 resource의 최대값
        Long nextId = maxId + 1;
        if (nextId < 1000) {
            nextId = 1000L;
        }
        if (nextId > 9999) {
            throw new IllegalStateException("ResourceAllocation ID 범위를 초과했습니다. (최대: 9999)");
        }
        return nextId;
    }
}

