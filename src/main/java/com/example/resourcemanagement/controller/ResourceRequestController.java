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
import com.example.resourcemanagement.entity.ResourceRequest;
import com.example.resourcemanagement.entity.ResourceType;
import com.example.resourcemanagement.service.AccountService;
import com.example.resourcemanagement.service.ResourceAllocationService;
import com.example.resourcemanagement.service.ResourceRequestService;
import com.example.resourcemanagement.service.ResourceService;
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

    ResourceRequestController(ResourceManagementApplication resourceManagementApplication) {
        this.resourceManagementApplication = resourceManagementApplication;
    }
    @PostMapping
    public ResponseEntity<List<ResourceRequest>> createResourceRequest(@RequestBody ResourceRequestDto dto) {
         resourceRequestService.createResourceRequest(dto);
        
        List<ResourceRequest> resourceRequests = resourceRequestRepository.findAll();
        return ResponseEntity.ok(resourceRequests);

    }
    
    @GetMapping("/{id}/approve")
    public ResponseEntity<Resource> approvedRequest(@PathVariable("id") Long id) {
        Resource resource = resourceAllocationService.approveResourceRequest(id);
        return ResponseEntity.ok(resource);
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
   
   
//   @PostMapping ("/gpu")
//  	public ResponseEntity<?> allocateGpuByModel(
//  			@RequestBody Map<String,Object> body) {
//	    Long accountId = extractLongValue(body, "accountId");
//	    Long resourceId = extractLongValue(body, "resourceId");
//	   String expiresAtStr = (String) body.get("expiresAt");
//	   String unit = (String) body.get("unit");
//	   Object quotaObj = body.get("quota");
//	   double quota = quotaObj instanceof Number ? ((Number) quotaObj).doubleValue() : 0.0;
//	   String modelId = (String) body.get("modelId");
//	   String typeStr = (String) body.get("type");
//	   ResourceType type = null;
//	   if (typeStr != null) {
//		   try {
//			   type = ResourceType.valueOf(typeStr.toLowerCase());
//		   } catch (IllegalArgumentException e) {
//			   throw new IllegalArgumentException("Invalid ResourceType: " + typeStr);
//		   }
//	   }
//	   
//	   ResourceRequest resourceRequest = new ResourceRequest();
//	   Account account  = new Account();
//	   account.setId(accountId);
//	   Resource resource = new Resource();
//	   resource.setId(resourceId);
//	   // expiresAt String을 LocalDateTime으로 파싱
//	   if (expiresAtStr != null && !expiresAtStr.isEmpty()) {
//		   try {
//			   java.time.OffsetDateTime offsetDateTime = java.time.OffsetDateTime.parse(
//				   expiresAtStr, 
//				   DateTimeFormatter.ISO_OFFSET_DATE_TIME
//			   );
//			   resourceRequest.setExpiresAt(offsetDateTime.toLocalDateTime());
//		   } catch (Exception e) {
//			   // 파싱 실패 시 null로 설정 (또는 예외 처리)
//			   resourceRequest.setExpiresAt(null);
//		   }
//	   }
//	   ResourceRequest createdRequest = resourceRequestService.allocateGpuByModel(resourceRequest);
//	   
//	    return ResponseEntity
//	            .status(HttpStatus.CREATED)
//	            .body(createdRequest);  // 생성된 엔티티 반환
//}



