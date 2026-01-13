package com.example.resourcemanagement.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.resourcemanagement.dto.ResourceRequestDto;
import com.example.resourcemanagement.entity.Account;
import com.example.resourcemanagement.entity.Resource;
import com.example.resourcemanagement.entity.ResourceAllocation;
import com.example.resourcemanagement.entity.ResourceBridge;
import com.example.resourcemanagement.entity.ResourceRequest;
import com.example.resourcemanagement.entity.ResourceType;
import com.example.resourcemanagement.repository.AccountRepository;
import com.example.resourcemanagement.repository.ResourceAllocationRepository;
import com.example.resourcemanagement.repository.ResourceBridgeRepository;
import com.example.resourcemanagement.repository.ResourceRepository;
import com.example.resourcemanagement.repository.ResourceRequestRepository;

import jakarta.transaction.Transactional;

@Service
public class ResourceRequestService {
	
	@Autowired
	AccountRepository accountRepository; 
	
	
	
	@Autowired
	ResourceRequestRepository resourceRequestRepository; 
	
	@Autowired
	ResourceAllocationService resourceAllocationService;
	
	@Autowired
	IdGenerationService idGenerationService;
	
	@Autowired
	ResourceBridgeService resourceBridgeService;
	
	@Autowired
	ResourceAllocationRepository resourceAllocationRepository;
	
	@Transactional
    public void createResourceRequest(ResourceRequestDto dto) {
        // 1. Account 조회
        Account account = accountRepository.findById(dto.getAccountId())
            .orElseThrow(() -> new IllegalArgumentException("Account not found"));
        
        // 2. ResourceRequest 생성 (ID: 10~99)
        // 3. 여러 개의 리소스를 순회하며 처리
        
        for(Resource resourceDto:dto.getResources()) {
        ResourceRequest resourceRequest = new ResourceRequest();
        Long requestId = idGenerationService.generateRequestId();
        resourceRequest.setStatus("required");
        resourceRequest.setId(requestId);
        resourceRequest.setAccount(account);
        resourceRequest.setRequestedAt(dto.getActivatedAt());
        resourceRequest.setExpiresAt(dto.getExpiredAt());
        resourceRequestRepository.save(resourceRequest);
        resourceBridgeService.createResourceAndBridge("request", requestId, resourceDto);
        }

        
    }
}
