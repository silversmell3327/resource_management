package com.example.resourcemanagement.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.resourcemanagement.entity.Account;
import com.example.resourcemanagement.entity.Resource;
import com.example.resourcemanagement.entity.ResourceAllocation;
import com.example.resourcemanagement.entity.ResourceBridge;
import com.example.resourcemanagement.entity.ResourceRequest;
import com.example.resourcemanagement.repository.AccountRepository;
import com.example.resourcemanagement.repository.ResourceAllocationRepository;
import com.example.resourcemanagement.repository.ResourceBridgeRepository;
import com.example.resourcemanagement.repository.ResourceRepository;
import com.example.resourcemanagement.repository.ResourceRequestRepository;

import jakarta.transaction.Transactional;

@Service
public class ResourceAllocationService {
	
	@Autowired
	AccountRepository accountRepository; 
	
	@Autowired
	ResourceRepository resourceRepository; 
	
	@Autowired
	ResourceAllocationRepository resourceAllocationRepository; 
	
	@Autowired
	ResourceRequestRepository resourceRequestRepository;
	
	@Autowired
	ResourceBridgeService resourceBridgeService;
	
	@Autowired
	ResourceBridgeRepository resourceBridgeRepository;
	
	@Autowired
	IdGenerationService idGenerationService; 
	
	/**
	 * ResourceRequest 승인 시 ResourceAllocation 생성
	 * 승인 시 allocation, bridge, resource 생성
	 */
	@Transactional
	public Resource approveResourceRequest(Long requestId) {
	    ResourceRequest resourceRequest = resourceRequestRepository.findById(requestId)
	        .orElseThrow(() -> new IllegalArgumentException("ResourceRequest not found"));

	    resourceRequest.setStatus("approved");
	    resourceRequestRepository.save(resourceRequest);
	    List<ResourceBridge> requestBridges =
	        resourceBridgeRepository.findByEntityAndEntityId("request", requestId);

	    Resource lastResource = null;
	    ResourceAllocation allocation = new ResourceAllocation();
	    allocation.setStatus("ACTIVE");
	    for (ResourceBridge requestBridge : requestBridges) {
	        Resource resource = requestBridge.getResource();

	        Long allocationId = idGenerationService.generateAllocationId();
	        allocation.setId(allocationId);
	        allocation.setResourceRequest(resourceRequest);
	        resourceAllocationRepository.save(allocation);

	        resourceBridgeService.createResourceAndBridge("allocation", allocationId, resource);
	        lastResource = resourceBridgeService.createOrAccumulateAccountResource(
	            resourceRequest.getAccount().getId(), resource
	        );
	    }
	    
	    return lastResource;
	}
	
	@Transactional
	public void createResourceAllocation(ResourceRequest resourceRequest){
		// 이 메서드는 기존 로직을 유지하되, 필요시 approveResourceRequest를 호출하도록 변경 가능
		approveResourceRequest(resourceRequest.getId());
	}
	


}
