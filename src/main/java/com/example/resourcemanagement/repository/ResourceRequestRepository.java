package com.example.resourcemanagement.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.resourcemanagement.entity.Account;
import com.example.resourcemanagement.entity.ResourceRequest;

@Repository
public interface ResourceRequestRepository extends JpaRepository<ResourceRequest, Long> {
	
	
}
