package com.example.resourcemanagement.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.resourcemanagement.entity.Account;
import com.example.resourcemanagement.entity.ResourceAllocation;
import com.example.resourcemanagement.entity.ResourceRequest;
import com.example.resourcemanagement.entity.ResourceType;

@Repository
public interface ResourceAllocationRepository extends JpaRepository<ResourceAllocation, Long> {
	ResourceAllocation findByAccount_IdAndResource_Id(
			Long accountId,
	        Long resourceId
	);
	
	List<ResourceAllocation> findAllByAccount_IdAndType(
			Long accountId,
		    ResourceType type
		);
	
	ResourceAllocation findByAccount_IdAndResource_IdAndModelId(
		    Long accountId,
		    Long resourceId,
		    String modelId
	);

}
