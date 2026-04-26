package com.lr.landregistration.service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.lr.landregistration.constant.EndPoints;
import com.lr.landregistration.dto.LandRequestDTO;
import com.lr.landregistration.pojo.AddLand;
import com.lr.landregistration.pojo.Buyer;
import com.lr.landregistration.repository.AddLandRepository;
import com.lr.landregistration.repository.BuyerRepository;
import com.lr.landregistration.repository.LandRequestRepository;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class BuyerService {

	private BuyerRepository buyerRepository;
	private LandRequestRepository landRequestRepository;
	private AddLandRepository addLandRepository;
	private BCryptPasswordEncoder bcryptPasswordEncoder;

	public BuyerService(BuyerRepository buyerRepository, 
			LandRequestRepository landRequestRepository,
			AddLandRepository addLandRepository, 
			BCryptPasswordEncoder bcryptPasswordEncoder) {
		super();
		this.buyerRepository = buyerRepository;
		this.landRequestRepository = landRequestRepository;
		this.addLandRepository = addLandRepository;
		this.bcryptPasswordEncoder = bcryptPasswordEncoder;
	}

	// buyer login validation
	public boolean validateBuyer(Buyer buyer) {
		Optional<Buyer> foundBuyer = buyerRepository
				.findByEmail(buyer.getEmail());
		log.info("buyer: " + foundBuyer);
		// Check if buyer exists and passwords match
		// if it's match Redirect on success otherwise Return on failed
		return foundBuyer.isPresent()
				&& bcryptPasswordEncoder.matches(buyer.getPassword(),
						foundBuyer.get().getPassword());
	}

	// save buyer registration data
	public void saveBuyer(Buyer buyer) throws IOException {
		log.info("saveBuyer invoked.");
		buyerRepository.save(buyer);
	}

	// buyer registration validation
	public String validateRegisterBuyer(@RequestParam String name, 
			@RequestParam Integer age, @RequestParam String city,
			@RequestParam String aadharNo, @RequestParam String panNo,
			@RequestParam("document") MultipartFile documentFile, 
			@RequestParam String buyerAddress, @RequestParam String email,
			@RequestParam String password, Model model) {
		log.info("validating registerBuyer");

		try {
			String encodePswd = bcryptPasswordEncoder.encode(password);
			log.info("encodePswd: " + encodePswd);
			password = encodePswd;

			// using builder pattern set values for fields
			Buyer buyer = Buyer.builder().name(name).age(age)
					.city(city).aadharNo(aadharNo).panNo(panNo)
					.buyerAddress(buyerAddress).email(email)
					.password(password).build();

			// Check if the document file is not empty
			if (!documentFile.isEmpty()) {
				// Convert MultipartFile to byte array
				log.info("creating documentFile: " + documentFile.getBytes());
				buyer.setDocument(documentFile.getBytes()); // Set the byte array in the buyer object
			} else {
				log.info("Document file is empty");
				model.addAttribute("error", "Document file is empty");
				return EndPoints.BUYER_REGISTRATION_PAGE; // Return to form with error
			}

			log.info("buyer: " + buyer);

			// Save buyer details
			saveBuyer(buyer);
			log.info("Successfully stored buyer details.");
		} catch (DataIntegrityViolationException vException) {
			log.error("DuplicateKeyException: " + vException.getMessage());
			model.addAttribute("error", 
					"A buyer with the same Aadhar number, email, "
							+ "or PAN already exists.");
			return EndPoints.BUYER_REGISTRATION_PAGE; // Return to form with error
		} catch (IOException ioException) {
			log.error("IOException: " + ioException.getMessage());
			model.addAttribute("error", "Error processing the document file. "
					+ "Please try again.");
			return EndPoints.BUYER_REGISTRATION_PAGE; // Return to form with error
		} catch (Exception exception) {
			log.error("Unexpected error: " + exception.getMessage());
			model.addAttribute("error", 
					"An unexpected error occurred. Please try again later.");
			return EndPoints.BUYER_REGISTRATION_PAGE; // Return to form with error
		}

		// Add a new buyer object to the model for the login page
		model.addAttribute("buyer", new Buyer());

		return EndPoints.BUYER_LOGIN_PAGE;
	}

	// get buyerId based on buyer login
	public Long getLoggedInBuyerId(Buyer buyer) {
		return buyerRepository.findBuyerIdByEmail(buyer.getEmail())
				.orElseThrow(() -> new RuntimeException("buyer not found"));
	}

	// Method to get the total number of buyers
	public Long getBuyerCount() {
		return buyerRepository.countAllBuyers();
	}

	// get all Buyers
	public List<Buyer> getAllBuyers() {
		return buyerRepository.findAll();
	}

	// save buyer land request
	public LandRequestDTO saveLandRequest(LandRequestDTO landRequest) {
		log.info("landRequest details saved successfully.");
		return landRequestRepository.save(landRequest);
	}

	// get all buyer land request
	public List<LandRequestDTO> getAllLandRequests() {
		log.info("getting all landRequest");
		return landRequestRepository.findAll();
	}

	// find buyer based on buyer id
	public Buyer findById(Long id) {
		log.info("buyer findById id: " + id);

		Optional<Buyer> optionalBuyer = buyerRepository.findById(id);
		if (optionalBuyer.isPresent()) {

			return optionalBuyer.get();
		} else {
			throw new IllegalArgumentException("Buyer not found with id: " + id);
		}
	}

	// fetch land details based on buyerId
	public List<LandRequestDTO> getLandDetailsByBuyerId(Long buyerId) {
		return landRequestRepository.findAllByBuyerId(buyerId);
	}

	// approved land request update status
	public void approveLandRequest(Long landRequestId) {
		log.info("approved land request");

		Optional<LandRequestDTO> optionalLandRequest = 
				landRequestRepository.findById(landRequestId);
		if (optionalLandRequest.isPresent()) {
			LandRequestDTO landRequest = optionalLandRequest.get();

			if (landRequest.getStatus().equals(EndPoints.STATUS_PENDING)) {
				landRequest.setStatus(EndPoints.STATUS_VERIFIED); // Update the status
			}

			log.info("landRequest: " + landRequest);
			landRequestRepository.save(landRequest); // Save the updated land request
		} else {
			log.warn("Land request with ID " + landRequestId + " not found.");
		}
	}

	// reject land request update status
	public void rejectLandRequest(Long landRequestId) {
		log.info("reject land request");

		Optional<LandRequestDTO> optionalLandRequest = 
				landRequestRepository.findById(landRequestId);
		if (optionalLandRequest.isPresent()) {
			LandRequestDTO landRequest = optionalLandRequest.get();

			if (landRequest.getStatus().equals(EndPoints.STATUS_PENDING)) {
				landRequest.setStatus(EndPoints.STATUS_REJECTED); // Update the status to REJECTED
			}

			log.info("landRequest: " + landRequest);
			landRequestRepository.save(landRequest); // Save the updated land request
		} else {
			log.warn("Land request with ID " + landRequestId + " not found.");
		}
	}

	// update status in add land
	@Transactional
	public void updateAddLandDetailsStatus() {
		log.info("update add-land status");

		// Fetch all statuses from land_request_details
		List<LandRequestDTO> verifiedRequests = 
				landRequestRepository.findByStatus(EndPoints.STATUS_VERIFIED);
		List<LandRequestDTO> rejectedRequests = 
				landRequestRepository.findByStatus(EndPoints.STATUS_REJECTED);

		// Update add_land_details based on land_request_details status
		for (LandRequestDTO request : verifiedRequests) {
			addLandRepository.updateStatusByPropertyId(EndPoints.STATUS_VERIFIED,
					request.getPropertyId());
		}

		for (LandRequestDTO request : rejectedRequests) {
			addLandRepository.updateStatusByPropertyId(EndPoints.STATUS_REJECTED, 
					request.getPropertyId());
		}
	}

	// get request land by status
	public List<AddLand> getAddLandDetails(String status) {
		log.info("getting add-land details");

		// Fetch all land request details from the database
		return addLandRepository.findByStatus(status);
	}

	// Method to get land details for buyerId and status
	public List<LandRequestDTO> getPaymentLand(Long buyerId, String status) {
		return landRequestRepository.findByBuyerIdAndStatus(buyerId, status);
	}

	// get buyer details based on propertyId
	public Buyer getBuyerDetails(Integer propertyId) {
		List<LandRequestDTO> reqLands = 
				landRequestRepository.findLandsByPropertyId(propertyId);
		for (LandRequestDTO land : reqLands) {
			Buyer buyer = land.getBuyer();
			if (buyer != null) {
				log.info("Buyer Name: " + buyer.getName());
				log.info("Buyer Address: " + buyer.getBuyerAddress());
				return buyer;
			} else {
				log.info("No buyer found for land ID: " + land.getId());
			}
		}
		return null;
	}
}