package com.example.resourcemanagement.dto;

import com.example.resourcemanagement.entity.ResourceType;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ResourceResponseDto {
    public ResourceType getType() {
		return type;
	}
	public void setType(ResourceType type) {
		this.type = type;
	}
	public String getModelId() {
		return modelId;
	}
	public void setModelId(String modelId) {
		this.modelId = modelId;
	}
	public String getUnit() {
		return unit;
	}
	public void setUnit(String unit) {
		this.unit = unit;
	}
	public Double getQuota() {
		return quota;
	}
	public void setQuota(Double quota) {
		this.quota = quota;
	}
	public Double getAllocated() {
		return allocated;
	}
	public void setAllocated(Double allocated) {
		this.allocated = allocated;
	}
	public Double getAvailable() {
		return available;
	}
	public void setAvailable(Double available) {
		this.available = available;
	}
	private ResourceType type;      // "cpu", "memory", "gpu", "storage"
    private String modelId;         // null 가능
    private String unit;            // "core", "GB", etc.
    private Double quota;           // 100.0
    private Double allocated;       // 0.0 (optional)
    private Double available;       // 100.0 (optional)
}
