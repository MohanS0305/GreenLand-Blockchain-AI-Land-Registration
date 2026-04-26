package com.lr.landregistration.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.lr.landregistration.pojo.AddLand;

import jakarta.transaction.Transactional;

@Repository
public interface AddLandRepository extends JpaRepository<AddLand, Long> {
	// Fetch all lands associated with a seller
	List<AddLand> findAllBySellerId(Long sellerId);

	// update the add land status by propertyId
	@Modifying
	@Transactional
	@Query("UPDATE AddLand a SET a.status = ?1"
			+ " WHERE a.propertyId = ?2")
	void updateStatusByPropertyId(String status, Integer propertyId);

	// Fetch all lands request by status
	List<AddLand> findByStatus(String status);

	// Fetch all lands associated with a seller and status VERIFIED
	List<AddLand> findAllBySellerIdAndStatus(Long sellerId, String status);
	
	// Fetch selled land count by sellerId 
	@Query("SELECT COUNT(a) FROM AddLand a WHERE a.seller.id = :sellerId "
			+ "AND a.status = :status")
	Long countBySellerIdAndStatus(@Param("sellerId") Long sellerId, 
			@Param("status") String status);
	
	// paid land update the status
    @Transactional
    @Modifying
    @Query("UPDATE AddLand lrd SET lrd.status = 'PURCHASED' "
    		+ "WHERE lrd.propertyId = :propertyId")
    void updateLandStatusToPurchased(Integer propertyId);
    
    // get seller name and address
    @Query("SELECT al FROM AddLand al JOIN al.seller s "
    		+ "WHERE al.propertyId = :propertyId")
	List<AddLand> findLandsByPropertyId(@Param("propertyId") Integer propertyId);
    
    // find land by property id
    AddLand findByPropertyId(Integer propertyId);
}