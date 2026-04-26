package com.lr.landregistration.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.lr.landregistration.pojo.TransactionResponse;

@Repository
public interface TransactionRepository extends 
JpaRepository<TransactionResponse, Long> {
}

