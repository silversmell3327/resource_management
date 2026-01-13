package com.example.resourcemanagement.controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.example.resourcemanagement.ResourceManagementApplication;
import com.example.resourcemanagement.dto.ActionPayloadDto;
import com.example.resourcemanagement.dto.ResourceRequestDto;
import com.example.resourcemanagement.entity.Account;
import com.example.resourcemanagement.entity.Resource;
import com.example.resourcemanagement.entity.ResourceAllocation;
import com.example.resourcemanagement.entity.ResourceRequest;
import com.example.resourcemanagement.entity.ResourceType;
import com.example.resourcemanagement.service.AccountService;
import com.example.resourcemanagement.service.ResourceAllocationService;
import com.example.resourcemanagement.service.ResourceRequestService;
import com.example.resourcemanagement.service.ResourceService;
import com.example.resourcemanagement.repository.ResourceAllocationRepository;
import com.example.resourcemanagement.repository.ResourceRequestRepository;

@RestController
@RequestMapping("/resource-requests")
public class ResourceRequestController {

    private final ResourceManagementApplication resourceManagementApplication;
	
	@Autowired
	ResourceRequestService resourceRequestService;
	
	@Autowired
	ResourceService resourceService;
	
	@Autowired
	ResourceAllocationService resourceAllocationService;
	
	@Autowired
	AccountService accountService;
	
	@Autowired
	ResourceRequestRepository resourceRequestRepository;
	
	@Autowired
	ResourceAllocationRepository resourceAllocationRepository;

    ResourceRequestController(ResourceManagementApplication resourceManagementApplication) {
        this.resourceManagementApplication = resourceManagementApplication;
    }
    @PostMapping
    public ResponseEntity<Void> createResourceRequest(@RequestBody ResourceRequestDto dto) {
         resourceRequestService.createResourceRequest(dto);
         return ResponseEntity.ok().build();
    }
    
    @GetMapping("/{id}/approve")
    public ResponseEntity<Void> approvedRequest(@PathVariable("id") Long id) {
        resourceAllocationService.approveResourceRequest(id);
        return ResponseEntity.ok().build();
    }
    
}
   



