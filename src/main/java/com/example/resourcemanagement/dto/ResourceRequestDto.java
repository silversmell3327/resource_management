package com.example.resourcemanagement.dto;

import java.time.LocalDateTime;
import java.util.List;

public class ResourceRequestDto {
    private Long accountId;
    private LocalDateTime requestedAt;
    private LocalDateTime expiresAt;
    private List<ResourceDto> resources;
    
    public Long getAccountId() { return accountId; }
    public void setAccountId(Long accountId) { this.accountId = accountId; }
    
    public LocalDateTime getRequestedAt() { return requestedAt; }
    public void setRequestedAt(LocalDateTime requestedAt) { this.requestedAt = requestedAt; }
    
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
    
    
    public List<ResourceDto> getResources() { return resources; }
    public void setResources(List<ResourceDto> resources) { this.resources = resources; }
    
    public static class ResourceDto {
        private String type;
        private String modelId;
        private Integer quota;
        private String unit;
        
        public String getUnit() {
			return unit;
		}
		public void setUnit(String unit) {
			this.unit = unit;
		}
		public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        
        public String getModelId() { return modelId; }
        public void setModelId(String modelId) { this.modelId = modelId; }
        
        public Integer getQuota() { return quota; }
        public void setQuota(Integer quota) { this.quota = quota; }
    }
}