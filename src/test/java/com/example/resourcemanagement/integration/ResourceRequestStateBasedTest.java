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

import org.json.JSONArray;
import org.json.JSONObject;
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
        JSONObject json = new JSONObject();
        json.put("accountId", testAccount.getId());
        json.put("activatedAt", "2025-12-31T09:00:00");
        json.put("expiredAt", "2026-01-31T09:00:00");

        JSONArray resources = new JSONArray();
        JSONObject resource1 = new JSONObject();
        resource1.put("type", "cpu");
        resource1.put("unit", "core");
        resource1.put("quota", 100);
        resources.put(resource1);
        
        JSONObject resource2 = new JSONObject();
        resource2.put("type", "cpu");
        resource2.put("unit", "core");
        resource2.put("quota", 100);
        resources.put(resource2);
        
        JSONObject resource3 = new JSONObject();
        resource3.put("type", "memory");
        resource3.put("unit", "GB");
        resource3.put("quota", 10);
        resources.put(resource3);
        
        JSONObject resource4 = new JSONObject();
        resource4.put("type", "gpu");
        resource4.put("unit", "EA");
        resource4.put("quota", 10);
        resources.put(resource4);
        json.put("resources", resources);

        // ========== When: action 실행 ==========
        String jsonString = json.toString();
        mockMvc.perform(post("/resource-requests")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonString))
                .andExpect(status().isOk());
        List<ResourceRequest> allRequests = resourceRequestRepository.findAll();
        for(int i = 0 ;i<allRequests.size();i++) {
        Long requestId = allRequests.get(i).getId();
        mockMvc.perform(get("/resource-requests/"+requestId+"/approve"))
                .andExpect(status().isOk());
        }
        // approve 후 다시 조회해서 status 확인
        List<ResourceRequest> approvedRequests = resourceRequestRepository.findAll();
        List<ResourceBridge> resourceBridge = resourceBridgeRepository.findByEntity("account");
        assertEquals(3, resourceBridge.size());
        assertEquals("approved", approvedRequests.get(0).getStatus());
    }
}