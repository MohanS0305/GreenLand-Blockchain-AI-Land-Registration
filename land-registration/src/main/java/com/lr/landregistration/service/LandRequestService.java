package com.lr.landregistration.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.lr.landregistration.constant.EndPoints;
import com.lr.landregistration.dto.LandRequestDTO;
import com.lr.landregistration.pojo.AddLand;
import com.lr.landregistration.repository.AddLandRepository;
import com.lr.landregistration.repository.LandRequestRepository;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class LandRequestService {

	private LandRequestRepository landRequestRepository;
	private AddLandRepository addLandRepository;

	public LandRequestService(LandRequestRepository landRequestRepository,
			AddLandRepository addLandRepository) {
		super();
		this.landRequestRepository = landRequestRepository;
		this.addLandRepository = addLandRepository;
	}

	// request land
	public void requestLand(LandRequestDTO landRequest) {
		log.info("landRequest: " + landRequest);

		// Check if the land request already exists using propertyId and surveyNo
		Optional<LandRequestDTO> existingLandRequest = landRequestRepository
				.findByPropertyIdAndSurveyNo(landRequest.getPropertyId(), 
						landRequest.getSurveyNo());

		if (existingLandRequest.isPresent()) {
			log.warn("Land request already exists: " + landRequest);
		} else {
			// Save the new land request to the database
			landRequestRepository.save(landRequest);
			log.info("LandRequest successfully added: " + landRequest);
		}

		// Update the status in the add_land_details table
		AddLand landDetails = 
				addLandRepository.findByPropertyId(landRequest.getPropertyId());

		if (landDetails != null) {
			landDetails.setStatus(EndPoints.STATUS_PENDING); // Update status to PENDING
			addLandRepository.save(landDetails); // Save the updated details
			log.info("Updated add_land_details status to PENDING for propertyId: " 
					+ landRequest.getPropertyId());
		} else {
			log.error("Land details not found for propertyId: " 
					+ landRequest.getPropertyId());
			throw new IllegalArgumentException("Land details not found.");
		}
	}

	// get request land by status
	public List<LandRequestDTO> getRequestedLands(String status) {
		log.info("getting requested land by status: " + status);

		// Fetch all land request details from the database
		return landRequestRepository.findByStatus(status);
	}

	// get request land count
	public Long getRequestedLandsCountByBuyerId(Long buyerId) {
		log.info("getting requested land  count by buyerId: " + buyerId);
		return landRequestRepository.countByBuyerId(buyerId); // return the count of land requests
	}

	@Transactional
	public void updateLandStatus(Long propertyId, String status) {
		log.info("Updating land status for propertyId: " 
				+ propertyId + " to status: " + status);
		landRequestRepository.updateStatusByPropertyId(status, propertyId);
	}

}
