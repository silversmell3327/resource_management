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

import com.example.resourcemanagement.dto.AccountResponseDto;
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
    private ObjectMapper objectMapper;
    
    @Autowired
    private DataSource dataSource;  // DB 연결 정보 확인용
    

    @BeforeEach
    void setUp() {
        // 테스트 전 초기화
        // 기존 DB 데이터는 유지하고 ResourceRequest만 삭제
        //resourceRequestRepository.deleteAll();
        /// accountRepository.deleteAll();  // 기존 Account 유지
        // resourceRepository.deleteAll();  // 실제 MySQL DB의 Resource는 유지
    }

    @Test
    @DisplayName("TC1: 자원요청 누적으로 계정 quota 누적")
    void testTC1_CreateResourceRequest_AccumulateQuota() throws Exception {
        Long accountId = 12L;
        Optional<Account> existingAccount = accountRepository.findById(accountId);
        if (!existingAccount.isPresent()) {
            List<Account> allAccounts = accountRepository.findAll();
            accountId = allAccounts.get(0).getId();
            existingAccount = Optional.of(allAccounts.get(0));
        }
        Account savedAccount = existingAccount.get();
        
        Long resourceId = 1L;
        Optional<Resource> existingResource = resourceRepository.findById(resourceId);
        
        // ========== When: action 실행 ==========
        Map<String, Object> actionPayload = new HashMap<>();
        actionPayload.put("accountId", accountId);
        actionPayload.put("resourceId", resourceId);  
        actionPayload.put("requestedAt", "2025-12-31T09:00:00+09:00");
        actionPayload.put("expiresAt", "2026-01-31T09:00:00+09:00");
        actionPayload.put("type","cpu");
        actionPayload.put("unit","core");
        actionPayload.put("quota", 100.0);
        MvcResult result = mockMvc.perform(post("/resource-requests")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(actionPayload)))
                .andExpect(status().isCreated())
                .andReturn();

        List<ResourceAllocation> requests = resourceAllocationRepository.findAll();
        //assertEquals(1, requests.size());
        
        ResourceAllocation createdRequest = requests.get(0);
        assertEquals(600, createdRequest.getQuota());
        
        // Account 확인
    }

}
