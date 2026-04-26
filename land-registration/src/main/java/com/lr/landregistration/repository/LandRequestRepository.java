package com.lr.landregistration.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.lr.landregistration.dto.LandRequestDTO;

public interface LandRequestRepository extends 
JpaRepository<LandRequestDTO, Long> {
	// Find land request by propertyId and surveyNo
	Optional<LandRequestDTO> findByPropertyIdAndSurveyNo(Integer propertyId, 
			Integer surveyNo);

	// Fetch all lands associated with a buyer
	List<LandRequestDTO> findAllByBuyerId(Long buyerId);

	// Fetch all lands request by status
	List<LandRequestDTO> findByStatus(String status);

	// Fetch the land count by buyerId
	Long countByBuyerId(Long buyerId);

	// update the status by propertyId
	@Modifying
	@Query("UPDATE LandRequestDTO l SET l.status = :status "
			+ "WHERE l.propertyId = :propertyId")
	void updateStatusByPropertyId(@Param("status") String status, 
			@Param("propertyId") Long propertyId);

	// Query to fetch land details based on buyerId and status
	List<LandRequestDTO> findByBuyerIdAndStatus(Long buyerId, String status);

	// paid land update the status
	@Transactional
	@Modifying
	@Query("UPDATE LandRequestDTO lrd SET lrd.status = 'PURCHASED' "
			+ "WHERE lrd.propertyId = :propertyId")
	void updateLandStatusToPurchased(Integer propertyId);

	// get buyer name and address
	@Query("SELECT lr FROM LandRequestDTO lr JOIN lr.buyer b "
			+ "WHERE lr.propertyId = :propertyId")
	List<LandRequestDTO> findLandsByPropertyId(
			@Param("propertyId") Integer propertyId);
}
