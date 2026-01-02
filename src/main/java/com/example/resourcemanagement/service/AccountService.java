package com.example.resourcemanagement.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.resourcemanagement.entity.Account;
import com.example.resourcemanagement.entity.Resource;
import com.example.resourcemanagement.entity.ResourceRequest;
import com.example.resourcemanagement.repository.AccountRepository;
import com.example.resourcemanagement.repository.ResourceRepository;
import com.example.resourcemanagement.repository.ResourceRequestRepository;

import jakarta.transaction.Transactional;

@Service
public class AccountService {
	
	@Autowired
	AccountRepository accountRepository; 
	
	@Autowired
	ResourceRepository resourceRepository; 
	
	
	@Autowired
	ResourceRequestRepository resourceRequestRepository; 
	
	@Transactional
	public Optional<Account> findAccount(Account account){
		
	     return accountRepository.findById(account.getId());	
	}

}
