package com.example.resourcemanagement.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.resourcemanagement.entity.Account;
import com.example.resourcemanagement.entity.Resource;
import com.example.resourcemanagement.entity.ResourceRequest;
import com.example.resourcemanagement.repository.AccountRepository;
import com.example.resourcemanagement.repository.ResourceAllocationRepository;
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
	ResourceRequestRepository resourceRequestRepository; 
	
	@Autowired
	ResourceAllocationService resourceAllocationService; 
	
	@Transactional
	public ResourceRequest createResourceRequest(ResourceRequest resourceRequest){
		
		Account account = accountRepository //계정이 있으면 통과
				.findById(resourceRequest.getAccount().getId())
	            .orElseThrow(() -> new IllegalArgumentException("Account not found: "));
	    Resource resource = resourceRepository //해당 자원이 있으면 통과
	    		.findById(resourceRequest.getResources().getId())
	    		.orElseThrow(() -> new IllegalArgumentException("Resource not found: "));
	    if (resourceRequest.getRequestedAt() == null) {
	    	resourceRequest.setRequestedAt(LocalDateTime.now());
	    }
	    ResourceRequest saved = resourceRequestRepository.save(resourceRequest);
		 resourceAllocationService.createResourceAllocation(saved);
		 
		 return saved;
	}

}
