package com.example.resourcemanagement.entity;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "resource")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class Project {
	public Long getId() {
		return Id;
	}

	public void setId(Long id) {
		Id = id;
	}

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

	public int getQuota() {
		return quota;
	}

	public void setQuota(int quota) {
		this.quota = quota;
	}

	public int getAllocated() {
		return allocated;
	}

	public void setAllocated(int allocated) {
		this.allocated = allocated;
	}

	public int getAvailable() {
		return available;
	}

	public void setAvailable(int available) {
		this.available = available;
	}
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAdmin() {
		return admin;
	}

	public void setAdmin(String admin) {
		this.admin = admin;
	}

	public Resource getResource() {
		return resource;
	}

	public void setResource(Resource resource) {
		this.resource = resource;
	}
	
	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}
	
	public Account getAccount() {
		return account;
	}

	public void setAccount(Account account) {
		this.account = account;
	}

	@Id
    @Column(name = "id")
    private Long Id;

	@Column(name = "name")
	private String name;
	
	@Column(name = "admin")
	private String admin;

	@Column(name = "unit")
	private String unit;
	
	@ManyToOne
	@JoinColumn(name = "account_id")
    private Account account;
	
	@ManyToOne
	@JoinColumn(name = "resource_id")
    private Resource resource;

    @Enumerated(EnumType.STRING) 
    @Column(nullable = false)
    private ResourceType type;
    
    @Column(name = "model_id")
    private String modelId;

    @Column(nullable = false)
    private int quota;
    

    @Column(nullable = false)
    private int allocated;

    @Column(nullable = false)
    private int available;
    
}
