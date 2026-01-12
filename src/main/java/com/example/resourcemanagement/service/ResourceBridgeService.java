package com.example.resourcemanagement.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.resourcemanagement.entity.Resource;
import com.example.resourcemanagement.entity.ResourceBridge;
import com.example.resourcemanagement.repository.ResourceBridgeRepository;
import com.example.resourcemanagement.repository.ResourceRepository;

import jakarta.transaction.Transactional;

/**
 * Resource와 ResourceBridge 생성을 담당하는 공통 서비스
 * request, allocation, account 간 Resource/Bridge 생성 로직을 공통화
 */
@Service
public class ResourceBridgeService {
    
    @Autowired
    private ResourceRepository resourceRepository;
    
    @Autowired
    private ResourceBridgeRepository resourceBridgeRepository;
    
    @Autowired
    private IdGenerationService idGenerationService;
    
    /**
     * Resource를 생성하고 해당 엔티티와 연결하는 Bridge를 생성
     * 
     * @param entityType "request", "allocation", "account" 등
     * @param entityId 엔티티의 ID
     * @param sourceResource 복사할 소스 Resource (type, modelId, quota, unit 복사)
     * @return 생성된 Resource
     */
    @Transactional
    public Resource createResourceAndBridge(String entityType, Long entityId, Resource sourceResource) {
        // Resource 생성
        Resource resource = createResourceFromSource(sourceResource);
        
        // Bridge 생성
        createBridge(entityType, entityId, resource);
        
        return resource;
    }
    
    /**
     * 소스 Resource를 기반으로 새로운 Resource 생성 (ID 자동 생성)
     */
    @Transactional
    public Resource createResourceFromSource(Resource sourceResource) {
        Resource resource = new Resource();
        Long resourceId = idGenerationService.generateResourceId();
        resource.setId(resourceId);
        resource.setType(sourceResource.getType());
        resource.setModelId(sourceResource.getModelId());
        resource.setQuota(sourceResource.getQuota());
        resource.setUnit(sourceResource.getUnit());
        resource.setAllocated(0);
        resource.setAvailable(sourceResource.getQuota());
        resourceRepository.save(resource);
        return resource;
    }
    
    /**
     * ResourceBridge 생성
     */
    @Transactional
    public ResourceBridge createBridge(String entityType, Long entityId, Resource resource) {
        ResourceBridge bridge = new ResourceBridge();
        bridge.setEntity(entityType);
        bridge.setEntityId(entityId);
        bridge.setResource(resource);
        resourceBridgeRepository.save(bridge);
        return bridge;
    }
    
    /**
     * Account의 기존 Resource를 찾아서 quota를 누적하거나, 없으면 새로 생성
     * 
     * @param accountId Account ID
     * @param sourceResource 누적할 Resource 정보
     * @return 기존 Resource가 있으면 기존 Resource, 없으면 새로 생성한 Resource
     */
    @Transactional
    public Resource createOrAccumulateAccountResource(Long accountId, Resource sourceResource) {
        // Account의 기존 Resource들을 조회
        List<ResourceBridge> accountBridges = resourceBridgeRepository.findByEntityAndEntityId("account", accountId);
        
        // 같은 type, modelId, unit을 가진 Resource 찾기
        Resource existingResource = null;
        
        for (ResourceBridge bridge : accountBridges) {
            Resource resource = bridge.getResource();
            if (isResourceMatching(resource, sourceResource)) {
                existingResource = resource;
                break; 
            }
        }
        
        if (existingResource != null) {
            // 기존 Resource의 quota와 available 증가
            int newQuota = existingResource.getQuota() + sourceResource.getQuota();
            int newAvailable = existingResource.getAvailable() + sourceResource.getQuota();
            existingResource.setQuota(newQuota);
            existingResource.setAvailable(newAvailable);
            resourceRepository.save(existingResource);
            return existingResource;
        } else {
            // 새로운 Resource와 Bridge 생성
            return createResourceAndBridge("account", accountId, sourceResource);
        }
    }
    
   
    private boolean isResourceMatching(Resource resource1, Resource resource2) {
        boolean typeMatches = resource1.getType().equals(resource2.getType());
        boolean modelIdMatches = (resource1.getModelId() == null && resource2.getModelId() == null) ||
                                 (resource1.getModelId() != null && resource1.getModelId().equals(resource2.getModelId()));
        boolean unitMatches = (resource1.getUnit() == null && resource2.getUnit() == null) ||
                              (resource1.getUnit() != null && resource1.getUnit().equals(resource2.getUnit()));
        
        return typeMatches && modelIdMatches && unitMatches;
    }
}

