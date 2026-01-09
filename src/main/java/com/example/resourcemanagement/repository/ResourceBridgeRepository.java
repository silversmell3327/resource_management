package com.example.resourcemanagement.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.resourcemanagement.entity.Resource;
import com.example.resourcemanagement.entity.ResourceBridge;
import com.example.resourcemanagement.entity.ResourceType;

@Repository
public interface ResourceBridgeRepository extends JpaRepository<ResourceBridge, Long> {
	List<ResourceBridge> findByEntityAndEntityId(String entity, Long entityId);
}
