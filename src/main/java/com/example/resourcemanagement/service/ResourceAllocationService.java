package com.example.resourcemanagement.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.resourcemanagement.entity.Account;
import com.example.resourcemanagement.entity.Resource;
import com.example.resourcemanagement.entity.ResourceAllocation;
import com.example.resourcemanagement.entity.ResourceRequest;
import com.example.resourcemanagement.repository.AccountRepository;
import com.example.resourcemanagement.repository.ResourceAllocationRepository;
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
	
	@Transactional
	public void createResourceAllocation(ResourceRequest resourceRequest){
		Long accountId = resourceRequest.getAccount().getId();
		Long resourcId = resourceRequest.getResources().getId();
		Double quota = resourceRequest.getQuota();
	    ResourceAllocation allocation = new ResourceAllocation();

		ResourceAllocation resourceAllocation = resourceAllocationRepository
				.findByAccount_IdAndResource_Id(accountId, resourcId);
		if(resourceAllocation==null) { //신규할당
		    allocation.setAccount(resourceRequest.getAccount());
		    allocation.setResource(resourceRequest.getResources());
		    allocation.setResourceRequest(resourceRequest);
		    allocation.setType(resourceRequest.getType());
		    allocation.setModelId(resourceRequest.getModelId());
		    allocation.setQuota(resourceRequest.getQuota());
		    allocation.setAllocated(0);
		    allocation.setAvailable(resourceRequest.getQuota());
			resourceAllocationRepository.save(allocation);
		}else { //이미 할당받은 경우
			resourceAllocation.setQuota(resourceAllocation.getQuota()+quota);
			resourceAllocation.setAvailable(resourceAllocation.getQuota()-resourceAllocation.getAllocated());
		}
	     resourceRequestRepository.save(resourceRequest);
	}


}
