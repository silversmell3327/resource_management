package com.example.resourcemanagement.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.resourcemanagement.entity.Resource;
import com.example.resourcemanagement.entity.ResourceType;

@Repository
public interface ResourceRepository extends JpaRepository<Resource, Long> {

}
