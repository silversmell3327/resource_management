package com.example.resourcemanagement.integration;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import com.example.resourcemanagement.dto.ActionPayloadDto;
import com.example.resourcemanagement.entity.Account;
import com.example.resourcemanagement.entity.Resource;
import com.example.resourcemanagement.entity.ResourceAllocation;
import com.example.resourcemanagement.entity.ResourceRequest;
import com.example.resourcemanagement.entity.ResourceType;
import com.example.resourcemanagement.repository.AccountRepository;
import com.example.resourcemanagement.repository.ResourceAllocationRepository;
import com.example.resourcemanagement.repository.ResourceRepository;
import com.example.resourcemanagement.repository.ResourceRequestRepository;
import com.example.resourcemanagement.service.ResourceRequestService;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 상태 기반 통합 테스트
 * initialState → action → expectedState 검증
 * 
 * 테스트 구조:
 * 1. initialState: 초기 상태 설정 (accounts, projects)
 * 2. action: 실행할 액션 (CreateResourceRequest)
 * 3. expectedState: 예상 결과 상태 검증
 */
@SpringBootTest
@AutoConfigureMockMvc
// @Transactional 제거 - 실제 MySQL DB 사용을 위해
@ActiveProfiles("test")
@DisplayName("ResourceRequest 상태 기반 통합 테스트")
class ResourceRequestStateBasedTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private ResourceRepository resourceRepository;
    
    @Autowired
    private ResourceAllocationRepository resourceAllocationRepository;

    @Autowired
    private ResourceRequestRepository resourceRequestRepository;
    
    @Autowired
    private ResourceRequestService resourceRequestService;

    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private DataSource dataSource;  // DB 연결 정보 확인용

	private Long accountId;
	


    @BeforeEach
    void setUp() {
        Account account = new Account();
        account.setAdmin("admin1");
        account.setName("Account-1");
        Account savedAccount = accountRepository.save(account);

        this.accountId = savedAccount.getId(); 
        Resource resource = new Resource();
        ArrayList type = new ArrayList();
        type.add(ResourceType.cpu);
        type.add(ResourceType.gpu);
        type.add(ResourceType.memory);
        type.add(ResourceType.storage);
        for(int i = 1 ;i<5;i++) {
        Resource res = new Resource();
        res.setId((long) i);
        res.setType((ResourceType) type.get(i-1));
        res.setQuota(100);
        res.setAvailable(100);
        res.setAllocated(0);
        resourceRepository.save(res);
        }
        
        // 초기 ResourceRequest를 위해 resource ID 1 (cpu) 조회
        Resource initialResource = resourceRepository.findById(1L)
        		.orElseThrow(() -> new IllegalArgumentException("Resource not found"));
        
        ResourceRequest initialRequest = new ResourceRequest();
        initialRequest.setAccount(savedAccount);
        initialRequest.setResources(initialResource);
        initialRequest.setType(ResourceType.cpu);
        initialRequest.setQuota(500.0);
        initialRequest.setUnit("core");
        initialRequest.setRequestedAt(LocalDateTime.now());

        resourceRequestService.createResourceRequest(initialRequest);
    }
    
    @AfterEach
    void tearDown() {
        // 테스트 후 생성된 데이터 정리
        resourceAllocationRepository.deleteAll();
        resourceRequestRepository.deleteAll();
        accountRepository.deleteAll();
        resourceRepository.deleteAll();
    }
    
    @Test
    @DisplayName("UC1_TC1") // 자원요청 누적으로 계정 quota 누적
    void testTC1_CreateResourceRequest_AccumulateQuota() throws Exception {

        ArrayList type = new ArrayList();
        type.add(ResourceType.cpu);
        type.add(ResourceType.gpu);
        type.add(ResourceType.memory);
        type.add(ResourceType.storage);
        
        // ========== When: action 실행 ==========
        Map<String, Object> actionPayload = new HashMap<>();
        actionPayload.put("accountId", accountId);
        actionPayload.put("requestedAt", "2025-12-31T09:00:00+09:00");
        actionPayload.put("expiresAt", "2026-01-31T09:00:00+09:00");
        actionPayload.put("unit","core");
        actionPayload.put("quota", 100.0);
        
        for(int i = 0;i<3;i++) {
        	 actionPayload.put("resourceId", i+1);  
        	 actionPayload.put("type",type.get(i));
        mockMvc.perform(post("/resource-requests")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(actionPayload)))
                .andExpect(status().isCreated())
                .andReturn();
        }
        List<ResourceAllocation> requests = resourceAllocationRepository.findAll();
        
        ResourceAllocation Request1 = requests.get(0);
        assertEquals(600, Request1.getQuota());
        
        ResourceAllocation Request2 = requests.get(1);
        assertEquals(100, Request2.getQuota());
        
    }

    @Test
    @DisplayName("UC1_TC2") //동일 타입 3회 요청 시 quota 누적(100*3=300)
    void testTC2_CreateResourceRequest_AccumulateQuota() throws Exception {

        // ========== When: action 실행 ==========
        Map<String, Object> actionPayload = new HashMap<>();
        actionPayload.put("accountId", accountId);
        actionPayload.put("resourceId", 1L);  
        actionPayload.put("requestedAt", "2025-12-31T09:00:00+09:00");
        actionPayload.put("expiresAt", "2026-01-31T09:00:00+09:00");
        actionPayload.put("type","cpu");
        actionPayload.put("unit","core");
        actionPayload.put("quota", 100.0);
        
        for(int i = 0;i<3;i++) {
        mockMvc.perform(post("/resource-requests")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(actionPayload)))
                .andExpect(status().isCreated())
                .andReturn();
        }
        List<ResourceAllocation> requests = resourceAllocationRepository.findAll();
        
        ResourceAllocation createdRequest = requests.get(0);
        assertEquals(800, createdRequest.getQuota());
        
    }

    @Test
    @DisplayName("UC1_TC3") //GPU는 modelId별로 별도 자원으로 누적
    void testTC3_CreateResourceRequest_AccumulateQuota() throws Exception {

    	ArrayList list = new ArrayList();
    	list.add("H100");
    	list.add("A100");
    	list.add("H100");
        // ========== When: action 실행 ==========
        Map<String, Object> actionPayload = new HashMap<>();
        actionPayload.put("accountId", accountId);
        actionPayload.put("resourceId", 3L);  
        actionPayload.put("requestedAt", "2025-12-31T09:00:00+09:00");
        actionPayload.put("expiresAt", "2026-01-31T09:00:00+09:00");
        actionPayload.put("type","gpu");
        actionPayload.put("quota", 1.0);
        
        for(int i = 0;i<list.size();i++) {
        	
       actionPayload.put("modelId",list.get(i));	
        mockMvc.perform(post("/resource-requests/gpu")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(actionPayload)))
                .andExpect(status().isCreated())
                .andReturn();
        }
        List<ResourceAllocation> requests = resourceAllocationRepository.findAllByAccount_IdAndType(accountId,ResourceType.gpu);
        
        assertEquals(2, requests.size());
    }
}
