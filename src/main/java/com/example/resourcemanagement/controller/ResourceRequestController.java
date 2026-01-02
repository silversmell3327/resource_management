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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.example.resourcemanagement.ResourceManagementApplication;
import com.example.resourcemanagement.dto.ActionPayloadDto;

import com.example.resourcemanagement.entity.Account;
import com.example.resourcemanagement.entity.Resource;
import com.example.resourcemanagement.entity.ResourceRequest;
import com.example.resourcemanagement.entity.ResourceType;
import com.example.resourcemanagement.service.AccountService;
import com.example.resourcemanagement.service.ResourceAllocationService;
import com.example.resourcemanagement.service.ResourceRequestService;
import com.example.resourcemanagement.service.ResourceService;

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

    ResourceRequestController(ResourceManagementApplication resourceManagementApplication) {
        this.resourceManagementApplication = resourceManagementApplication;
    }
	
   @PostMapping
   	public ResponseEntity<?> resourceRequest(
   			@RequestBody Map<String,Object> body) {
	    Long accountId = extractLongValue(body, "accountId");
	    Long resourceId = extractLongValue(body, "resourceId");
	   String expiresAtStr = (String) body.get("expiresAt");
	   String unit = (String) body.get("unit");
	   Object quotaObj = body.get("quota");
	   double quota = quotaObj instanceof Number ? ((Number) quotaObj).doubleValue() : 0.0;
	   
	   String typeStr = (String) body.get("type");
	   ResourceType type = null;
	   if (typeStr != null) {
		   try {
			   type = ResourceType.valueOf(typeStr.toLowerCase());
		   } catch (IllegalArgumentException e) {
			   throw new IllegalArgumentException("Invalid ResourceType: " + typeStr);
		   }
	   }
	   
	   ResourceRequest resourceRequest = new ResourceRequest();
	   Account account  = new Account();
	   account.setId(accountId);
	   Resource resource = new Resource();
	   resource.setId(resourceId);
	   resourceRequest.setAccount(account);
	   resourceRequest.setResources(resource);
	   resourceRequest.setQuota(quota);
	   resourceRequest.setUnit(unit);
	   resourceRequest.setType(type);
	   // expiresAt String을 LocalDateTime으로 파싱
	   if (expiresAtStr != null && !expiresAtStr.isEmpty()) {
		   try {
			   java.time.OffsetDateTime offsetDateTime = java.time.OffsetDateTime.parse(
				   expiresAtStr, 
				   DateTimeFormatter.ISO_OFFSET_DATE_TIME
			   );
			   resourceRequest.setExpiresAt(offsetDateTime.toLocalDateTime());
		   } catch (Exception e) {
			   // 파싱 실패 시 null로 설정 (또는 예외 처리)
			   resourceRequest.setExpiresAt(null);
		   }
	   }
	   ResourceRequest createdRequest = resourceRequestService.createResourceRequest(resourceRequest);
	   
	    return ResponseEntity
	            .status(HttpStatus.CREATED)
	            .body(createdRequest);  // 생성된 엔티티 반환
 }

// 헬퍼 메서드
private Long extractLongValue(Map<String, Object> body, String key) {
    Object obj = body.get(key);
    if (obj instanceof Long) {
        return (Long) obj;
    } else if (obj instanceof Integer) {
        return ((Integer) obj).longValue();
    } else if (obj instanceof Number) {
        return ((Number) obj).longValue();
    } else {
        throw new IllegalArgumentException(key + " must be a number");
    }
}
}
