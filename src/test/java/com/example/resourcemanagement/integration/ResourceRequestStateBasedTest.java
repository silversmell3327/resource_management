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
        
        // 멀티라인 문자열로 JSON 정의
        String jsonString = """
            {
                "accountId": %d,
                "activatedAt": "2025-12-31T09:00:00",
                "expiredAt": "2026-01-31T09:00:00",
                "resources": [
                    {
                        "type": "cpu",
                        "unit": "core",
                        "quota": 100
                    },
                    {
                        "type": "cpu",
                        "unit": "core",
                        "quota": 100
                    },
                    {
                        "type": "memory",
                        "unit": "GB",
                        "quota": 10
                    },
                    {
                        "type": "gpu",
                        "unit": "EA",
                        "quota": 10
                    }
                ]
            }
            """.formatted(testAccount.getId());
        
        // JSONObject로 파싱 (필요한 경우)
        JSONObject json = new JSONObject(jsonString);

        // ========== When: action 실행 ==========
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
        int cpu_quota=0;
        for(int i = 0 ;i<resourceBridge.size();i++) {
        	if(resourceBridge.get(i).getResource().getType().equals(ResourceType.cpu)) {
        		cpu_quota=resourceBridge.get(i).getResource().getQuota();
        	}
        }
        assertEquals(3, resourceBridge.size());
        assertEquals("approved", approvedRequests.get(0).getStatus());
        assertEquals(200,cpu_quota); //TC2 테스트 코드
    }
    
//    "initialState": {
//        "accounts": [
//          {
//            "id": "A1",
//            "name": "Account-1",
//            "admin": "admin1",
//            "resources": [
//              { "type": "cpu", "modelId": null, "unit": "core", "quota": 100.0, "allocated": 0.0, "available": 100.0 }
//            ],
//            "projectIds": []
//          }
//        ],
//        "projects": []
//      },
//      "action": {
//        "type": "CreateResourceRequestBatch",
//        "payload": {
//          "accountId": "A1",
//          "requests": [
//            {
//              "id": "RR2",
//              "requestedAt": "2025-12-31T09:10:00+09:00",
//              "expiresAt": "2026-01-31T09:10:00+09:00",
//              "message": "cpu 100",
//              "resources": [{ "type": "cpu", "modelId": null, "unit": "core", "quota": 100.0 }]
//            },
//            {
//              "id": "RR3",
//              "requestedAt": "2025-12-31T09:20:00+09:00",
//              "expiresAt": "2026-01-31T09:20:00+09:00",
//              "message": "cpu 100",
//              "resources": [{ "type": "cpu", "modelId": null, "unit": "core", "quota": 100.0 }]
//            }
//          ]
//        }
//      },
//      "expectedState": {
//        "accounts": [
//          {
//            "id": "A1",
//            "resources": [
//              { "type": "cpu", "modelId": null, "unit": "core", "quota": 300.0, "allocated": 0.0, "available": 300.0 }
//            ]
//          }
//        ]
//      }
//    }
    
    @Test
    @DisplayName("TC2: 동일 타입 3회 요청 시 quota 누적")
    void testTC2_ResourceRequestAccumulateAccountQuota() throws Exception {
        // ========== Given: initialState 설정 ==========
        // Account A1 생성 (resources 빈 배열)
        Account testAccount = new Account();
        testAccount.setName("Account-1");
        testAccount.setAdmin("admin1");
        Account savedAccount = accountRepository.save(testAccount);
        Long accountId = savedAccount.getId();
        
        // 멀티라인 문자열로 JSON 배열 정의
        String jsonArrayString = """
            [
                {
                    "accountId": %d,
                    "activatedAt": "2025-12-31T09:00:00",
                    "expiredAt": "2026-01-31T09:00:00",
                    "resources": [
                        {
                            "type": "cpu",
                            "unit": "core",
                            "quota": 100
                        }
                    ]
                },
                {
                    "accountId": %d,
                    "activatedAt": "2025-12-31T09:00:00",
                    "expiredAt": "2026-01-31T09:00:00",
                    "resources": [
                        {
                            "type": "cpu",
                            "unit": "core",
                            "quota": 100
                        }
                    ]
                }
            ]
            """.formatted(testAccount.getId(), testAccount.getId());
        
        // JSONArray로 파싱
        JSONArray requests = new JSONArray(jsonArrayString);

        // ========== When: action 실행 ==========
        for(int i = 0 ;i<requests.length();i++) {
        JSONObject request = requests.getJSONObject(i);
        mockMvc.perform(post("/resource-requests")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request.toString()))
                .andExpect(status().isOk());
        }
        List<ResourceRequest> allRequests = resourceRequestRepository.findAll();
        for(int i = 0 ;i<allRequests.size();i++) {
        Long requestId = allRequests.get(i).getId();
        mockMvc.perform(get("/resource-requests/"+requestId+"/approve"))
                .andExpect(status().isOk());
        }
        // approve 후 다시 조회해서 status 확인
        List<ResourceRequest> approvedRequests = resourceRequestRepository.findAll();
        List<ResourceBridge> resourceBridge = resourceBridgeRepository.findByEntity("account");
        int cpu_quota=0;
        for(int i = 0 ;i<resourceBridge.size();i++) {
        	if(resourceBridge.get(i).getResource().getType().equals(ResourceType.cpu)) {
        		cpu_quota=resourceBridge.get(i).getResource().getQuota();
        	}
        }
        assertEquals(200,cpu_quota); //TC2 테스트 코드
    }
    
//    {
//        "id": "TC3",
//        "title": "GPU는 modelId별로 별도 자원으로 누적",
//        "initialState": {
//          "accounts": [
//            { "id": "A2", "name": "Account-2", "admin": "admin2", "resources": [], "projectIds": [] }
//          ],
//          "projects": []
//        },
//        "action": {
//          "type": "CreateResourceRequestBatch",
//          "payload": {
//            "accountId": "A2",
//            "requests": [
//              {
//                "id": "RR-G1",
//                "requestedAt": "2025-12-31T09:00:00+09:00",
//                "expiresAt": "2026-01-31T09:00:00+09:00",
//                "message": "gpu H100 2",
//                "resources": [{ "type": "gpu", "modelId": "H100", "unit": "card", "quota": 2.0 }]
//              },
//              {
//                "id": "RR-G2",
//                "requestedAt": "2025-12-31T09:05:00+09:00",
//                "expiresAt": "2026-01-31T09:05:00+09:00",
//                "message": "gpu A100 1",
//                "resources": [{ "type": "gpu", "modelId": "A100", "unit": "card", "quota": 1.0 }]
//              }
//            ]
//          }
//        },
//        "expectedState": {
//          "accounts": [
//            {
//              "id": "A2",
//              "resources": [
//                { "type": "gpu", "modelId": "H100", "unit": "card", "quota": 2.0, "allocated": 0.0, "available": 2.0 },
//                { "type": "gpu", "modelId": "A100", "unit": "card", "quota": 1.0, "allocated": 0.0, "available": 1.0 }
//              ]
//            }
//          ]
//        }
//      },
    
    @Test
    @DisplayName("TC3: GPU는 modelId별로 별도 자원으로 누적")
    void testTC3_ResourceRequestAccumulateAccountQuota() throws Exception {
        // ========== Given: initialState 설정 ==========
        // Account A1 생성 (resources 빈 배열)
        Account testAccount = new Account();
        testAccount.setName("Account-1");
        testAccount.setAdmin("admin1");
        Account savedAccount = accountRepository.save(testAccount);
        Long accountId = savedAccount.getId();
        
        // 멀티라인 문자열로 JSON 배열 정의
        String jsonArrayString = """
            [
                {
                    "accountId": %d,
                    "activatedAt": "2025-12-31T09:00:00",
                    "expiredAt": "2026-01-31T09:00:00",
                    "resources": [
                        {
                            "type": "gpu",
                            "modelId":"H100",
                            "unit": "core",
                            "quota": 2.0,
                            "available": 2.0
                        }
                    ]
                },
                {
                    "accountId": %d,
                    "activatedAt": "2025-12-31T09:00:00",
                    "expiredAt": "2026-01-31T09:00:00",
                    "resources": [
                        {
                            "type": "gpu",
                            "modelId": "A100",
                            "unit": "core",
                            "quota": 1.0,
                            "available": 1.0
                        }
                    ]
                }
            ]
            """.formatted(testAccount.getId(), testAccount.getId());
        
        // JSONArray로 파싱
        JSONArray requests = new JSONArray(jsonArrayString);

        // ========== When: action 실행 ==========
        for(int i = 0 ;i<requests.length();i++) {
        JSONObject request = requests.getJSONObject(i);
        mockMvc.perform(post("/resource-requests")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request.toString()))
                .andExpect(status().isOk());
        }
        List<ResourceRequest> allRequests = resourceRequestRepository.findAll();
        for(int i = 0 ;i<allRequests.size();i++) {
        Long requestId = allRequests.get(i).getId();
        mockMvc.perform(get("/resource-requests/"+requestId+"/approve"))
                .andExpect(status().isOk());
        }
        // approve 후 다시 조회해서 status 확인
        List<ResourceBridge> resourceBridge = resourceBridgeRepository.findByEntity("account");
        
        int gpu_count = 0;
        Resource h100Resource = null;
        Resource a100Resource = null;
        
        for(int i = 0; i < resourceBridge.size(); i++) {
            Resource resource = resourceBridge.get(i).getResource();
            if(resource.getType().equals(ResourceType.gpu)) {
                gpu_count++;
                if("H100".equals(resource.getModelId())) {
                    h100Resource = resource;
                } else if("A100".equals(resource.getModelId())) {
                    a100Resource = resource;
                }
            }
        }
        
        // 검증
        assertEquals(2, gpu_count);        
        // H100 GPU 검증
        assertEquals("H100", h100Resource.getModelId());
        assertEquals(2, h100Resource.getQuota());
        assertEquals(2, h100Resource.getAvailable());
        assertEquals(0, h100Resource.getAllocated());
        
        // A100 GPU 검증
        assertEquals("A100", a100Resource.getModelId());
        assertEquals(1, a100Resource.getQuota());
        assertEquals(1, a100Resource.getAvailable());
        assertEquals(0, a100Resource.getAllocated()); 
    }
}