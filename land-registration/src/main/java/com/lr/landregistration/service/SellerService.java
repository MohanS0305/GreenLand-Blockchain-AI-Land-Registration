package com.lr.landregistration.service;

import java.io.IOException;
import java.math.BigDecimal;
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
import com.lr.landregistration.pojo.Seller;
import com.lr.landregistration.repository.AddLandRepository;
import com.lr.landregistration.repository.LandRequestRepository;
import com.lr.landregistration.repository.SellerRepository;

import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class SellerService {

	private SellerRepository sellerRepository;
	private AddLandRepository addLandRepository;
	private LandRequestRepository landRequestRepository;
	private BuyerService buyerService;
	private BCryptPasswordEncoder bcryptPasswordEncoder;

	public SellerService(SellerRepository sellerRepository,
			AddLandRepository addLandRepository,
			LandRequestRepository landRequestRepository,
			BuyerService buyerService,
			BCryptPasswordEncoder bcryptPasswordEncoder) {
		super();
		this.sellerRepository = sellerRepository;
		this.addLandRepository = addLandRepository;
		this.landRequestRepository = landRequestRepository;
		this.buyerService = buyerService;
		this.bcryptPasswordEncoder = bcryptPasswordEncoder;
	}

	// seller login validation
	public boolean validateSeller(Seller seller) {
		Optional<Seller> foundSeller = 
				sellerRepository.findByEmail(seller.getEmail());
		log.info("seller: " + foundSeller);
		// Check if seller exists and passwords match
		// if it's match Redirect on success otherwise Return on failed
		return foundSeller.isPresent() && bcryptPasswordEncoder
				.matches(seller.getPassword(), foundSeller.get().getPassword());
	}

	// save seller registration data
	public void saveSeller(Seller seller) throws IOException {
		log.info("saveSeller invoked.");
		sellerRepository.save(seller);
	}

	// validate seller registration page
	public String validateRegisterSeller(@RequestParam String name, 
			@RequestParam Integer age,
			@RequestParam String city, @RequestParam String aadharNo,
			@RequestParam String panNo,
			@RequestParam("document") MultipartFile documentFile,
			@RequestParam String sellerAddress, @RequestParam String email, 
			@RequestParam String password, Model model) {
		log.info("validating registerSeller");

		try {
			String encodePswd = bcryptPasswordEncoder.encode(password);
			log.info("encodePswd: " + encodePswd);
			password = encodePswd;

			// using builder pattern set values for fields
			Seller seller = Seller.builder().name(name).age(age)
					.city(city).aadharNo(aadharNo).panNo(panNo)
					.sellerAddress(sellerAddress).email(email)
					.password(password).build();
			// Check if the document file is not empty
			if (!documentFile.isEmpty()) {
				// Convert MultipartFile to byte array
				log.info("creating documentFile: " + documentFile.getBytes());
				seller.setDocument(documentFile.getBytes()); // Set the byte array in the buyer object
			} else {
				log.info("Id Proof Document file is empty");
				model.addAttribute("error", "Document file is empty");
				return EndPoints.SELLER_REGISTRATION_PAGE; // Return to form with error
			}

			log.info("seller: " + seller);
			// save seller registration page data
			saveSeller(seller);
			log.info("Successfully stored seller details.");

		} catch (DataIntegrityViolationException vException) {
			log.error("DuplicateKeyException: " + vException.getMessage());
			model.addAttribute("error", "A seller with the same Aadhar number, "
					+ "email, or PAN already exists.");
			return EndPoints.SELLER_REGISTRATION_PAGE; // Return to form with error
		} catch (IOException ioException) {
			log.error("IOException: " + ioException.getMessage());
			model.addAttribute("error", "Error processing the document file. "
					+ "Please try again.");
			return EndPoints.SELLER_REGISTRATION_PAGE; // Return to form with error
		} catch (Exception exception) {
			log.error("Unexpected error: " + exception.getMessage());
			model.addAttribute("error", "An unexpected error occurred. "
					+ "Please try again later.");
			return EndPoints.SELLER_REGISTRATION_PAGE; // Return to form with error
		}

		// Add a new seller object to the model for the login page
		model.addAttribute("seller", new Seller());

		return EndPoints.SELLER_LOGIN_PAGE;
	}

	// validate land registration page
	public String validateLandRegister(@RequestParam Integer area, // Area of the land
			@RequestParam String city, // City where the land is located
			@RequestParam String state, // State where the land is located
			@RequestParam BigDecimal price, // Price of the land
			@RequestParam Integer propertyId, // Property PID number
			@RequestParam Integer physicalSurveyNo, // Physical Survey Number
			@RequestParam String aadharNo, // Aadhar Number of the seller
			@RequestParam("landImage") MultipartFile landImageFile, // Land image file (MultipartFile for file upload)
			HttpSession session, Model model) {
		log.info("Validating landRegister");
		try {

			// Fetch the seller using Aadhar number or email
			Optional<Seller> optionalSeller = 
					sellerRepository.findByAadharNo(aadharNo); // Assuming aadharNo is unique
			if (!optionalSeller.isPresent()) {
				model.addAttribute("error", 
						"Seller not found with the provided Aadhar number.");
				return EndPoints.SELLER_ADD_LAND_PAGE; // Return to the form with an error
			}

			Seller seller = optionalSeller.get(); // Get the seller object

			// using builder pattern set values for fields
			AddLand addLand = AddLand.builder().area(area)
					.city(city).state(state).price(price).propertyId(propertyId)
					.physicalSurveyNo(physicalSurveyNo).aadharNo(aadharNo)
					.status("INITIATED").seller(seller).build();
			// Convert MultipartFile to byte array and set it in AddLand
			if (!landImageFile.isEmpty()) {
				addLand.setLandImage(landImageFile.getBytes());
				log.info("landImageBytes: " + landImageFile.getBytes());
			} else {
				log.info("landImage file is empty");
				model.addAttribute("error", "landImage file is empty");
				return EndPoints.SELLER_ADD_LAND_PAGE; // Return to the form with an error
			}
			log.info("addLand: " + addLand + "getSeller(): " 
					+ addLand.getSeller());

			// Save land details
			saveLand(addLand, addLand.getSeller().getId());
			log.info("Successfully stored land details.");

			// Get sellerId from session
			Long sellerId = (Long) session.getAttribute("sellerId");

			List<AddLand> landList;
			if (sellerId != null) {
				// Fetch land details for the logged-in seller
				landList = getLandDetailsBySellerId(sellerId);
				model.addAttribute("landList", landList);
				log.info("Land details for sellerId {}: {}", sellerId, landList);
				// Fetch the buyer count and add it to the model
				Long buyerCount = buyerService.getBuyerCount();

				model.addAttribute("buyerCount", buyerCount);
				// get the land count and add it to the model
				model.addAttribute("landCount", landList.size());
			} else {
				log.error("Seller ID not found in session.");
				// Redirect to login page if sellerId is not in session
				return EndPoints.SELLER_LOGIN_PAGE;
			}

		} catch (DataIntegrityViolationException vException) {
			log.error("DuplicateKeyException: " + vException.getMessage());
			model.addAttribute("error", "A land record with the same "
					+ "Aadhar number, email, or PAN already exists.");
			return EndPoints.SELLER_ADD_LAND_PAGE; // Return to form with error
		} catch (IOException ioException) {
			log.error("IOException: " + ioException.getMessage());
			model.addAttribute("error", "Error processing the document file."
					+ " Please try again.");
			return EndPoints.SELLER_ADD_LAND_PAGE; // Return to form with error
		} catch (Exception exception) {
			log.error("Unexpected error: " + exception.getMessage());
			model.addAttribute("error", "An unexpected error occurred."
					+ " Please try again later.");
			return EndPoints.SELLER_ADD_LAND_PAGE; // Return to form with error
		}

		return EndPoints.SELLER_DASHBOARD_PAGE;
	}

	// Save add-land registration data
	public void saveLand(AddLand land, Long sellerId) throws IOException {
		log.info("saving land details: {}", land);
		log.info("Seller ID: " + sellerId); 

		// Fetch the sellerId from the repository
		Optional<Seller> optionalSeller = sellerRepository.findById(sellerId);

		// Check if the sellerId is present or not
		if (!optionalSeller.isPresent()) {
			log.info("Associated seller: {}", optionalSeller.orElse(null)); 
			throw new IllegalArgumentException("Seller not found");
		}

		// Set the seller to the AddLand instance
		land.setSeller(optionalSeller.get()); // Get the Seller from the Optional

		addLandRepository.save(land); // Use addLandRepository to save land details
	}

	// get add-land details
	public List<AddLand> getAllLandDetails() {
		log.info("getting all land details");
		return addLandRepository.findAll();
	}

	// get sellerId based on seller login
	public Long getLoggedInSellerId(Seller seller) {
		log.info("getting login sellerId");
		return sellerRepository.findSellerIdByEmail(seller.getEmail())
				.orElseThrow(() -> new RuntimeException("Seller not found"));
	}

	// fetch land details based on sellerId
	public List<AddLand> getLandDetailsBySellerId(Long sellerId) {
		log.info("getting land details by sellerId: " + sellerId);
		return addLandRepository.findAllBySellerId(sellerId);
	}

	// get the total number of sellers
	public Long getSellerCount() {
		return sellerRepository.countAllSellers();
	}

	// get the total number of land register count
	public Long getTotalLandCount() {
		log.info("getting total land count");
		return addLandRepository.count(); // This returns the count of all entries in add_land_details
	}

	// fetch land request details based on sellerId and VERIFIED status
	public List<AddLand> getRequestLandDetailsBySellerIdAndStatus(Long sellerId, 
			String status) {
		log.info("getting request land details by sellerId: " 
				+ sellerId + "status: " + status);
		return addLandRepository.findAllBySellerIdAndStatus(sellerId, status);
	}

	// update status in add land
	@Transactional
	public void updateAddLandDetailsStatus() {
		log.info("updating add-land details by status");

		// Fetch all statuses from land_request_details
		List<LandRequestDTO> successfulRequests = 
				landRequestRepository.findByStatus(EndPoints.STATUS_SUCCESS);
		List<LandRequestDTO> failedRequests = 
				landRequestRepository.findByStatus(EndPoints.STATUS_FAILED);

		// Update add_land_details based on land_request_details status
		for (LandRequestDTO request : successfulRequests) {
			addLandRepository.updateStatusByPropertyId(EndPoints.STATUS_SUCCESS, 
					request.getPropertyId());
		}

		for (LandRequestDTO request : failedRequests) {
			addLandRepository.updateStatusByPropertyId(EndPoints.STATUS_FAILED,
					request.getPropertyId());
		}
	}

	// get the selled land count
	public long getSoldLandCountBySellerId(Long sellerId) {
		log.info("sell land count by sellerId: " + sellerId);
		return addLandRepository.countBySellerIdAndStatus(sellerId, 
				EndPoints.STATUS_PURCHASED);
	}

	// get seller details based on propertyId
	public Seller getSellerDetails(Integer propertyId) {
		List<AddLand> lands = 
				addLandRepository.findLandsByPropertyId(propertyId);
		for (AddLand land : lands) {
			Seller seller = land.getSeller();
			if (seller != null) {
				log.info("Seller Name: " + seller.getName());
				log.info("Seller Address: " + seller.getSellerAddress());
				return seller;
			} else {
				log.info("No seller found for land ID: " + land.getId());
			}
		}
		return null;
	}

}
