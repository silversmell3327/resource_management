package com.example.resourcemanagement.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.example.resourcemanagement.entity.Resource;

public class ResourceRequestDto {
    public Long getAccountId() {
		return accountId;
	}
	public void setAccountId(Long accountId) {
		this.accountId = accountId;
	}
	public LocalDateTime getActivatedAt() {
		return activatedAt;
	}
	public void setActivatedAt(LocalDateTime activatedAt) {
		this.activatedAt = activatedAt;
	}
	public LocalDateTime getExpiredAt() {
		return expiredAt;
	}
	public void setExpiredAt(LocalDateTime expiredAt) {
		this.expiredAt = expiredAt;
	}
	public List<Resource> getResources() {
		return resources;
	}
	public void setResources(List<Resource> resources) {
		this.resources = resources;
	}
	private Long accountId;
    private LocalDateTime activatedAt;
    private LocalDateTime expiredAt;
    private List<Resource> resources;  
}
