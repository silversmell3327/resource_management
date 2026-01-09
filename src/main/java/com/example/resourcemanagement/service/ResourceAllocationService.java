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
	ResourceBridgeRepository resourceBridgeRepository;
	
	@Autowired
	IdGenerationService idGenerationService; 
	
	/**
	 * ResourceRequest 승인 시 ResourceAllocation 생성
	 * 승인 시 allocation, bridge, resource 생성
	 */
	@Transactional
	public void approveResourceRequest(Long requestId) {
		// 1. ResourceRequest 조회
		ResourceRequest resourceRequest = resourceRequestRepository.findById(requestId)
			.orElseThrow(() -> new IllegalArgumentException("ResourceRequest not found"));
		
		Account account = resourceRequest.getAccount();
		Long accountId = account.getId();
		
		// 2. Request에 연결된 Resource들을 브릿지로 조회
		List<ResourceBridge> requestBridges = resourceBridgeRepository.findByEntityAndEntityId("request", requestId);
		
		for (ResourceBridge requestBridge : requestBridges) {
			Resource requestResource = requestBridge.getResource();
			
			// 3. Allocation 생성 (ID: 1000~9999)
			ResourceAllocation allocation = new ResourceAllocation();
			Long allocationId = idGenerationService.generateAllocationId();
			allocation.setId(allocationId);
			allocation.setResourceRequest(resourceRequest);
			resourceAllocationRepository.save(allocation);
			
			// 4. Allocation용 Resource 생성 (ID: 100~999)
			Resource allocationResource = new Resource();
			Long allocationResourceId = idGenerationService.generateResourceId();
			allocationResource.setId(allocationResourceId);
			allocationResource.setType(requestResource.getType());
			allocationResource.setModelId(requestResource.getModelId());
			allocationResource.setQuota(requestResource.getQuota());
			allocationResource.setUnit(requestResource.getUnit());
			allocationResource.setAllocated(0);
			allocationResource.setAvailable(requestResource.getQuota());
			resourceRepository.save(allocationResource);
			
			// 5. 브릿지 생성 (entity: "allocation", entity_id: allocation_id, resource_id: resource_id)
			ResourceBridge allocationBridge = new ResourceBridge();
			allocationBridge.setEntity("allocation");
			allocationBridge.setEntityId(allocationId);
			allocationBridge.setResource(allocationResource);
			resourceBridgeRepository.save(allocationBridge);
			
			// 6. Account용 Resource 생성 또는 기존 Resource quota 누적
			// Account의 기존 Resource들을 조회
			List<ResourceBridge> accountBridges = resourceBridgeRepository.findByEntityAndEntityId("account", accountId);
			
			// 같은 type, modelId, unit을 가진 Resource 찾기
			Resource existingAccountResource = null;
			ResourceBridge existingAccountBridge = null;
			for (ResourceBridge bridge : accountBridges) {
				Resource resource = bridge.getResource();
				boolean typeMatches = resource.getType().equals(requestResource.getType());
				boolean modelIdMatches = (resource.getModelId() == null && requestResource.getModelId() == null) ||
				                         (resource.getModelId() != null && resource.getModelId().equals(requestResource.getModelId()));
				boolean unitMatches = (resource.getUnit() == null && requestResource.getUnit() == null) ||
				                      (resource.getUnit() != null && resource.getUnit().equals(requestResource.getUnit()));
				
				if (typeMatches && modelIdMatches && unitMatches) {
					existingAccountResource = resource;
					existingAccountBridge = bridge;
					break;
				}
			}
			
			if (existingAccountResource != null) {
				// 기존 Resource의 quota와 available 증가
				int newQuota = existingAccountResource.getQuota() + requestResource.getQuota();
				int newAvailable = existingAccountResource.getAvailable() + requestResource.getQuota();
				existingAccountResource.setQuota(newQuota);
				existingAccountResource.setAvailable(newAvailable);
				resourceRepository.save(existingAccountResource);
				// 새로운 브릿지는 생성하지 않음 (기존 브릿지 사용)
			} else {
				// 새로운 Resource와 브릿지 생성
				Resource accountResource = new Resource();
				Long accountResourceId = idGenerationService.generateResourceId();
				accountResource.setId(accountResourceId);
				accountResource.setType(requestResource.getType());
				accountResource.setModelId(requestResource.getModelId());
				accountResource.setQuota(requestResource.getQuota());
				accountResource.setUnit(requestResource.getUnit());
				accountResource.setAllocated(0);
				accountResource.setAvailable(requestResource.getQuota());
				resourceRepository.save(accountResource);
				
				// 브릿지 생성 (entity: "account", entity_id: account_id, resource_id: resource_id)
				ResourceBridge accountBridge = new ResourceBridge();
				accountBridge.setEntity("account");
				accountBridge.setEntityId(accountId);
				accountBridge.setResource(accountResource);
				resourceBridgeRepository.save(accountBridge);
			}
		}
	}
	
	@Transactional
	public void createResourceAllocation(ResourceRequest resourceRequest){
		// 이 메서드는 기존 로직을 유지하되, 필요시 approveResourceRequest를 호출하도록 변경 가능
		approveResourceRequest(resourceRequest.getId());
	}
	


}
