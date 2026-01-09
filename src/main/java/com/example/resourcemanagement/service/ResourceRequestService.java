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
	ResourceRepository resourceRepository; 
	
	@Autowired
	ResourceBridgeRepository resourceBridgeRepository; 
	
	
	@Autowired
	ResourceRequestRepository resourceRequestRepository; 
	
	@Autowired
	ResourceAllocationService resourceAllocationService;
	
	@Autowired
	IdGenerationService idGenerationService;
	
	@Autowired
	ResourceAllocationRepository resourceAllocationRepository;
	
	@Transactional
    public Long createResourceRequest(ResourceRequestDto dto) {
        // 1. Account 조회
        Account account = accountRepository.findById(dto.getAccountId())
            .orElseThrow(() -> new IllegalArgumentException("Account not found"));
        
        // 2. ResourceRequest 생성 (ID: 10~99)
        ResourceRequest resourceRequest = new ResourceRequest();
        Long requestId = idGenerationService.generateRequestId();
        resourceRequest.setId(requestId);
        resourceRequest.setAccount(account);
        resourceRequest.setRequestedAt(dto.getRequestedAt());
        resourceRequest.setExpiresAt(dto.getExpiresAt());
        resourceRequestRepository.save(resourceRequest);
        
        // 3. Resource 생성 및 브릿지 연결
        for (ResourceRequestDto.ResourceDto resourceDto : dto.getResources()) {
            // Resource 생성 (ID: 100~999)
            Resource resource = new Resource();
            Long resourceId = idGenerationService.generateResourceId();
            resource.setId(resourceId);
            resource.setType(ResourceType.valueOf(resourceDto.getType()));
            resource.setModelId(resourceDto.getModelId());
            resource.setQuota(resourceDto.getQuota());
            resource.setUnit(resourceDto.getUnit());
            resource.setAllocated(0);
            resource.setAvailable(resourceDto.getQuota());
            resourceRepository.save(resource);
            
            // 브릿지 생성 (entity: "request", entity_id: request_id, resource_id: resource_id)
            ResourceBridge bridge = new ResourceBridge();
            bridge.setEntity("request");
            bridge.setEntityId(requestId);
            bridge.setResource(resource);
            resourceBridgeRepository.save(bridge);
        }
        
        return resourceRequest.getId();
    }
	

}
