package com.example.resourcemanagement.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.resourcemanagement.entity.Account;
import com.example.resourcemanagement.entity.ResourceAllocation;
import com.example.resourcemanagement.entity.ResourceRequest;

@Repository
public interface ResourceAllocationRepository extends JpaRepository<ResourceAllocation, Long> {
	ResourceAllocation findByAccount_IdAndResource_Id(
			Long accountId,
	        Long resourceId
	);

}
