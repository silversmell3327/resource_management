package com.example.resourcemanagement.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.example.resourcemanagement.entity.Account;
import com.example.resourcemanagement.repository.AccountRepository;
import com.example.resourcemanagement.repository.ResourceRequestRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 통합 테스트 예시
 * @SpringBootTest: 전체 Spring 컨텍스트를 로드하여 통합 테스트 수행
 * @AutoConfigureMockMvc: MockMvc 자동 설정
 * @Transactional: 테스트 후 롤백하여 데이터 정리
 * @ActiveProfiles("test"): test 프로파일 사용 (별도 application-test.properties 설정 가능)
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
@DisplayName("ResourceRequest 통합 테스트")
class ResourceRequestIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private ResourceRequestRepository resourceRequestRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Account testAccount;

    @BeforeEach
    void setUp() {
        // 테스트용 데이터 준비
        testAccount = new Account();
        testAccount.setName("통합 테스트 사용자");
        testAccount.setAdmin("admin");
        accountRepository.save(testAccount);
    }

    @Test
    @DisplayName("전체 플로우 테스트 - Controller -> Service -> Repository")
    void testFullFlow() throws Exception {
        // Given: 요청 데이터
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("accountId", testAccount.getId());
        requestBody.put("message", "통합 테스트 메시지");

        // When & Then: API 호출
        mockMvc.perform(post("/resource-requests")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isCreated());

        // DB에서 확인 (Repository 직접 사용)
        // 주의: @Transactional 때문에 실제로는 롤백됨
    }
}
