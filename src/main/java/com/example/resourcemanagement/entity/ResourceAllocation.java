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


	    public Long getId() {
			return id;
		}

		public void setId(Long id) {
			this.id = id;
		}

		public ResourceRequest getResourceRequest() {
			return resourceRequest;
		}

		public void setResourceRequest(ResourceRequest resourceRequest) {
			this.resourceRequest = resourceRequest;
		}
      @Id
      @GeneratedValue(strategy = GenerationType.IDENTITY)
	   private Long id;

	    @ManyToOne(fetch = FetchType.LAZY)
	    @JoinColumn(name = "resource_request_id", nullable = false)
	    private ResourceRequest resourceRequest;
	    

}
