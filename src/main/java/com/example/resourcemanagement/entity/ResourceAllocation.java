package com.example.resourcemanagement.entity;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "resource_allocation")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResourceAllocation {
	  @Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    private Long id;

	    /* ======================
	       Account (ManyToOne)
	       ====================== */
	    @ManyToOne(fetch = FetchType.LAZY)
	    @JoinColumn(name = "account_id", nullable = false)
	    private Account account;


	    public Long getId() {
			return id;
		}

		public void setId(Long id) {
			this.id = id;
		}

		public Account getAccount() {
			return account;
		}

		public void setAccount(Account account) {
			this.account = account;
		}

		public Resource getResource() {
			return resource;
		}

		public void setResource(Resource resource) {
			this.resource = resource;
		}

		public ResourceRequest getResourceRequest() {
			return resourceRequest;
		}

		public void setResourceRequest(ResourceRequest resourceRequest) {
			this.resourceRequest = resourceRequest;
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

		public Double getQuota() {
			return quota;
		}

		public void setQuota(Double quota) {
			this.quota = quota;
		}

		public int getAllocated() {
			return allocated;
		}

		public void setAllocated(int allocated) {
			this.allocated = allocated;
		}

		public Double getAvailable() {
			return available;
		}

		public void setAvailable(Double available) {
			this.available = available;
		}

		@ManyToOne(fetch = FetchType.LAZY)
	    @JoinColumn(name = "resource_id", nullable = false)
	    private Resource resource;

	    @ManyToOne(fetch = FetchType.LAZY)
	    @JoinColumn(name = "resource_request_id", nullable = false)
	    private ResourceRequest resourceRequest;
	    
	    @Enumerated(EnumType.STRING)  // ⭐ 핵심
	    @Column(nullable = false)
	    private ResourceType type;

	    @Column(name = "model_id")
	    private String modelId;

	    @Column(nullable = false)
	    private Double quota;

	    @Column(nullable = false)
	    private int allocated;

	    @Column(nullable = false)
	    private Double available;
}
