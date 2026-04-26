package com.lr.landregistration.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.lr.landregistration.pojo.AddLand;
import com.lr.landregistration.pojo.LandTransaction;

@Repository
public interface LandTransactionRepository extends 
JpaRepository<LandTransaction, Integer> {
	@Query("SELECT al FROM AddLand al JOIN al.seller s "
			+ "WHERE al.propertyId = :propertyId")
	List<AddLand> findLandsByPropertyId(@Param("propertyId") Integer propertyId);

}
