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
import com.example.resourcemanagement.entity.ResourceBridge;
import com.example.resourcemanagement.entity.ResourceRequest;
import com.example.resourcemanagement.entity.ResourceType;
import com.example.resourcemanagement.repository.AccountRepository;
import com.example.resourcemanagement.repository.ResourceAllocationRepository;
import com.example.resourcemanagement.repository.ResourceBridgeRepository;
import com.example.resourcemanagement.repository.ResourceRepository;
import com.example.resourcemanagement.repository.ResourceRequestRepository;
import com.example.resourcemanagement.service.ResourceRequestService;
import com.example.resourcemanagement.service.ResourceAllocationService;
import com.example.resourcemanagement.dto.ResourceRequestDto;
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
    private ResourceBridgeRepository resourceBridgeRepository;
    
    @Autowired
    private ResourceAllocationRepository resourceAllocationRepository;

    @Autowired
    private ResourceRequestRepository resourceRequestRepository;
    
    @Autowired
    private ResourceRequestService resourceRequestService;
    
    @Autowired
    private ResourceAllocationService resourceAllocationService;

    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private DataSource dataSource;  // DB 연결 정보 확인용

	private Long accountId;
	
	private Account account;


    @BeforeEach
    void setUp() {
    }
    
    @AfterEach
    void tearDown() {
        // 테스트 후 생성된 데이터 정리
        resourceAllocationRepository.deleteAll();
        resourceRequestRepository.deleteAll();
        resourceBridgeRepository.deleteAll();
        accountRepository.deleteAll();
        resourceRepository.deleteAll();
    }

    
    /**
     * TC1: 자원요청 누적으로 계정 quota 누적
     * 
     * JSON 테스트 케이스:
     * {
     *   "id": "TC1",
     *   "title": "자원요청 누적으로 계정 quota 누적",
     *   "initialState": {
     *     "accounts": [{"id": "A1", "name": "Account-1", "admin": "admin1", "resources": [], "projectIds": []}],
     *     "projects": []
     *   },
     *   "action": {
     *     "type": "CreateResourceRequest",
     *     "payload": {
     *       "accountId": "A1",
     *       "requestedAt": "2025-12-31T09:00:00+09:00",
     *       "expiresAt": "2026-01-31T09:00:00+09:00",
     *       "message": "cpu 100",
     *       "resources": [{"type": "cpu", "modelId": null, "unit": "core", "quota": 100.0}]
     *     }
     *   },
     *   "expectedState": {
     *     "accounts": [{
     *       "id": "A1",
     *       "resources": [{"type": "cpu", "modelId": null, "unit": "core", "quota": 100.0, "allocated": 0.0, "available": 100.0}]
     *     }]
     *   }
     * }
     */
    @Test
    @DisplayName("TC1: 자원요청 누적으로 계정 quota 누적")
    void testTC1_ResourceRequestAccumulateAccountQuota() throws Exception {
        // ========== Given: initialState 설정 ==========
        // Account A1 생성 (resources 빈 배열)
        Account testAccount = new Account();
        testAccount.setName("Account-1");
        testAccount.setAdmin("admin1");
        Account savedAccount = accountRepository.save(testAccount);
        Long accountId = savedAccount.getId();
        
        // ========== When: action 실행 ==========
        // 첫 번째 CreateResourceRequest 실행 (cpu 100)
        ResourceRequestDto requestDto1 = new ResourceRequestDto();
        requestDto1.setAccountId(accountId);
        requestDto1.setRequestedAt(LocalDateTime.of(2025, 12, 31, 9, 0));
        requestDto1.setExpiresAt(LocalDateTime.of(2026, 1, 31, 9, 0));

        ResourceRequestDto.ResourceDto resourceDto1 = new ResourceRequestDto.ResourceDto();
        resourceDto1.setType("cpu");
        resourceDto1.setModelId(null);
        resourceDto1.setQuota(100);
        resourceDto1.setUnit("core");
        
        List<ResourceRequestDto.ResourceDto> resources1 = new ArrayList<>();
        resources1.add(resourceDto1);
        requestDto1.setResources(resources1);
        
        // 첫 번째 자원요청 생성 및 승인
        Long requestId1 = resourceRequestService.createResourceRequest(requestDto1);
        resourceAllocationService.approveResourceRequest(requestId1);
        
        // 두 번째 CreateResourceRequest 실행 (같은 타입 cpu 50)
        ResourceRequestDto requestDto2 = new ResourceRequestDto();
        requestDto2.setAccountId(accountId);
        requestDto2.setRequestedAt(LocalDateTime.of(2025, 12, 31, 10, 0));
        requestDto2.setExpiresAt(LocalDateTime.of(2026, 1, 31, 10, 0));

        ResourceRequestDto.ResourceDto resourceDto2 = new ResourceRequestDto.ResourceDto();
        resourceDto2.setType("cpu");
        resourceDto2.setModelId(null);
        resourceDto2.setQuota(50);
        resourceDto2.setUnit("core");
        
        List<ResourceRequestDto.ResourceDto> resources2 = new ArrayList<>();
        resources2.add(resourceDto2);
        requestDto2.setResources(resources2);
        
        // 두 번째 자원요청 생성 및 승인 (같은 타입이므로 quota 누적)
        Long requestId2 = resourceRequestService.createResourceRequest(requestDto2);
        resourceAllocationService.approveResourceRequest(requestId2);
        
        // ========== Then: expectedState 검증 ==========
        
        // 4. Account A1의 resources 검증
        List<ResourceBridge> accountBridges = resourceBridgeRepository.findByEntityAndEntityId("account", accountId);
        ResourceBridge accountBridge = accountBridges.get(0);

        Resource accountResource = accountBridge.getResource();
        
        // Resource 검증 (quota가 누적되어 150이어야 함)
        assertEquals(ResourceType.cpu, accountResource.getType(), "cpu");
        assertNull(accountResource.getModelId(), "null");
        assertEquals(150, accountResource.getQuota(), "150");
        assertEquals("core", accountResource.getUnit(), "core");
        assertEquals(0, accountResource.getAllocated(), "0");
        assertEquals(150, accountResource.getAvailable(), "150");
        

    }
}