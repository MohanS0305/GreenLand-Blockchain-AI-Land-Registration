package com.lr.landregistration.controller;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;

import org.json.JSONObject;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lr.landregistration.constant.EndPoints;
import com.lr.landregistration.dto.LandRequestDTO;
import com.lr.landregistration.pojo.AddLand;
import com.lr.landregistration.pojo.Admin;
import com.lr.landregistration.pojo.Buyer;
import com.lr.landregistration.pojo.LandTransaction;
import com.lr.landregistration.pojo.Seller;
import com.lr.landregistration.pojo.TransactionResponse;
import com.lr.landregistration.service.AdminService;
import com.lr.landregistration.service.BuyerService;
import com.lr.landregistration.service.LandRequestService;
import com.lr.landregistration.service.SellerService;
import com.lr.landregistration.service.TransactionService;
import com.lr.landregistration.utility.PdfGenerator;

import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Controller
@RequestMapping(EndPoints.LANDREGISTER)
public class LandController {

	private AdminService adminService;
	private BuyerService buyerService;
	private SellerService sellerService;
	private LandRequestService landRequestService;
	private TransactionService transactionService;
	private PdfGenerator pdfGenerator;
	private final WebClient webClient;

	public LandController(AdminService adminService,
			BuyerService buyerService, SellerService sellerService,
			LandRequestService landRequestService, 
			TransactionService transactionService, PdfGenerator pdfGenerator,
			WebClient.Builder webClientBuilder) {
		super();
		this.adminService = adminService;
		this.buyerService = buyerService;
		this.sellerService = sellerService;
		this.landRequestService = landRequestService;
		this.transactionService = transactionService;
		this.pdfGenerator = pdfGenerator;
		this.webClient = webClientBuilder
				.baseUrl("http://localhost:3000").build();
	}

	// home page
	@GetMapping(EndPoints.EMPTY_SLASH)
	public String showHomePage() {
		log.info("home page invoked");
		return EndPoints.INDEX_PAGE;
	}

	// Admin Login page
	@GetMapping(EndPoints.ADMIN_LOGIN)
	public String showAdminLogin() {
		log.info("admin login page invoked");
		return EndPoints.ADMIN_LOGIN_PAGE;
	}

	// Admin login page validation
	@PostMapping(EndPoints.ADMIN_LOGIN)
	public String validateAdminLogin(@ModelAttribute Admin admin, Model model) {
		log.info("email: " + admin.getEmail() 
		+ " |password: " + admin.getPassword());

		if (adminService.validateAdmin(admin)) {
			// invoke admin Dashboard
			return getAdminDashboard(model);
		}

		// Invalid credentials, return to login page with error message
		model.addAttribute("error", "Invaild email & password");
		log.error("invaild email & password");

		return EndPoints.ADMIN_LOGIN_PAGE;
	}

	// buyer Login page
	@GetMapping(EndPoints.BUYER_LOGIN)
	public String showBuyerLogin(Model model) {
		log.info("buyer login page invoked");
		model.addAttribute("buyer", new Buyer());

		return EndPoints.BUYER_LOGIN_PAGE;
	}

	// buyer Login page validation
	@PostMapping(EndPoints.BUYER_LOGIN)
	public String validateBuyerLogin(@ModelAttribute Buyer buyer, 
			HttpSession session, Model model) {
		log.info("Buyer email: " + buyer.getEmail() 
		+ " Buyer password: " + buyer.getPassword());

		if (buyerService.validateBuyer(buyer)) {

			// Fetch the logged-in seller's ID from the service
			Long buyerId = buyerService.getLoggedInBuyerId(buyer);
			log.info("buyerId: " + buyerId);

			// Store sellerId in session
			session.setAttribute("buyerId", buyerId);

			// buyerDashboard page invoked
			showBuyerDashboard(session, model);

			return EndPoints.BUYER_DASHBOARD_PAGE;
		}

		log.error("Invalid email or password");
		// Add error message to model and return to login page
		model.addAttribute("error", "Invalid Email or Password");

		return EndPoints.BUYER_LOGIN_PAGE; // Return to login page
	}

	// buyer registration page
	@GetMapping(EndPoints.BUYER_REGISTRATION)
	public String showBuyerRegistrationForm(Model model) {
		log.info("buyer registration page invoked");
		model.addAttribute("buyer", new Buyer());

		return EndPoints.BUYER_REGISTRATION_PAGE;
	}

	// save buyer registration page data
	@PostMapping(EndPoints.BUYER_REGISTRATION)
	public String validateBuyerRegister(@RequestParam String name, 
			@RequestParam Integer age, @RequestParam String city,
			@RequestParam String aadharNo, @RequestParam String panNo,
			@RequestParam("document") MultipartFile documentFile,
			@RequestParam String buyerAddress,
			@RequestParam String email, @RequestParam String password,
			Model model)
					throws SQLIntegrityConstraintViolationException {
		log.info("Buyer registration invoked");

		// validate buyer register page
		return buyerService.validateRegisterBuyer(
				name, age, city, aadharNo, panNo, 
				documentFile, buyerAddress, email,
				password, model);
	}

	// seller Login page
	@GetMapping(EndPoints.SELLER_LOGIN)
	public String showSellerLogin(Model model) {
		log.info("seller login page invoked");
		model.addAttribute("seller", new Seller());

		return EndPoints.SELLER_LOGIN_PAGE; // Return to seller login page
	}

	// seller Login page validation
	@PostMapping(EndPoints.SELLER_LOGIN)
	public String validateSellerLogin(@ModelAttribute Seller seller, 
			HttpSession session, Model model) {
		log.info("Seller email: " + seller.getEmail() 
		+ " Seller password: " + seller.getPassword());

		if (sellerService.validateSeller(seller)) {
			// Fetch the logged-in seller's ID from the service
			Long sellerId = sellerService.getLoggedInSellerId(seller);
			log.info("sellerId: " + sellerId);

			// Store sellerId in session
			session.setAttribute("sellerId", sellerId);

			// sellerDashboard page invoked
			showSellerDashboard(session, model);

			return EndPoints.SELLER_DASHBOARD_PAGE;
		}

		log.error("Invalid email or password");
		// Add error message to model and return to login page
		model.addAttribute("error", "Invalid Email or Password");

		return EndPoints.SELLER_LOGIN_PAGE; // Return to seller login page
	}

	// seller registration page
	@GetMapping(EndPoints.SELLER_REGISTRATION)
	public String showSellerRegistrationForm(Model model) {
		log.info("seller registration page invoked");
		model.addAttribute("seller", new Seller());

		// Return to seller registration page
		return EndPoints.SELLER_REGISTRATION_PAGE; 
	}

	// validate seller register page
	@PostMapping(EndPoints.SELLER_REGISTRATION)
	public String validateSellerRegister(@RequestParam String name, 
			@RequestParam Integer age,
			@RequestParam String city, 
			@RequestParam String aadharNo, @RequestParam String panNo,
			@RequestParam("document") MultipartFile documentFile, 
			@RequestParam String sellerAddress,
			@RequestParam String email, @RequestParam String password, 
			Model model)
					throws SQLIntegrityConstraintViolationException {
		log.info("Seller registration invoked");

		return sellerService.validateRegisterSeller(name, age, city, 
				aadharNo, panNo, documentFile, sellerAddress,
				email, password, model);
	}

	// add-land(land registration page)
	@GetMapping(EndPoints.SELLER_ADD_LAND)
	public String showAddLand(Model model) {
		log.info("add land page invoked");
		model.addAttribute("addland", new LandRequestDTO());

		// Return to seller land register page
		return EndPoints.SELLER_ADD_LAND_PAGE; 
	}

	// add-land register validation
	@PostMapping(EndPoints.SELLER_ADD_LAND)
	public String validateRegisterLand(
			@RequestParam Integer area, // Area of the land
			@RequestParam String city, // City where the land is located
			@RequestParam String state, // State where the land is located
			@RequestParam BigDecimal price, // Price of the land
			@RequestParam Integer propertyId, // Property PID number
			@RequestParam Integer physicalSurveyNo, // Physical Survey Number
			@RequestParam String aadharNo, // Aadhar Number of the seller
			@RequestParam("landImage") 
			MultipartFile landImageFile, // Land image file
			HttpSession session, Model model) 
					throws SQLIntegrityConstraintViolationException {

		log.info("land registration invoked");

		// Validate and save the land registration details
		return sellerService.validateLandRegister(area, city, 
				state, price, propertyId, physicalSurveyNo, aadharNo,
				landImageFile, session, model);
	}

	// Seller Dashboard
	@GetMapping(EndPoints.SELLER_DASHBOARD)
	public String showSellerDashboard(HttpSession session, Model model) {
		// Get sellerId from session
		Long sellerId = (Long) session.getAttribute("sellerId");

		List<AddLand> landList;
		if (sellerId != null) {
			// Fetch land details for the logged-in seller
			landList = sellerService.getLandDetailsBySellerId(sellerId);
			model.addAttribute("landList", landList);
			log.info("Land details for sellerId {}: {}", sellerId, landList);

			// Fetch the buyer count
			Long buyerCount = buyerService.getBuyerCount();
			model.addAttribute("buyerCount", buyerCount);

			// Fetch the selled land count
			Long sellerCount = sellerService.getSoldLandCountBySellerId(sellerId);
			log.info("sellerCount: " + sellerCount);
			model.addAttribute("sellCount", sellerCount);
			// get the land count and add it to the model
			model.addAttribute("landCount", landList.size());
		} else {
			log.error("Seller ID not found in session.");
			// Redirect to login page if sellerId is not in session
			return EndPoints.SELLER_LOGIN_PAGE;
		}
		return EndPoints.SELLER_DASHBOARD_PAGE;
	}

	// buyer dashBoard
	@GetMapping(EndPoints.BUYER_DASHBOARD)
	public String showBuyerDashboard(HttpSession session, Model model) {
		// Get buyerId from session
		Long buyerId = (Long) session.getAttribute("buyerId");

		List<AddLand> landList;
		if (buyerId != null) {
			// Fetch the requested lands (this method exists in LandService)
			landList = buyerService.getAddLandDetails(EndPoints.STATUS_INITIATED);
			model.addAttribute("landList", landList);
			log.info("land details landList: " + landList);

			// Fetch the seller count
			Long sellerCount = sellerService.getSellerCount();
			model.addAttribute("sellerCount", sellerCount);
			// Get the land count from the AddLand table
			Long landCount = sellerService.getTotalLandCount();
			model.addAttribute("landCount", landCount);
			// Fetch the request count by buyerId
			Long requestCount = landRequestService
					.getRequestedLandsCountByBuyerId(buyerId);
			model.addAttribute("requestCount", requestCount);
		}
		return EndPoints.BUYER_DASHBOARD_PAGE;
	}

	// buyer land request logic
	@PostMapping(EndPoints.BUYER_REQUEST_LAND)
	public String requestLand(@RequestBody LandRequestDTO landRequest, 
			HttpSession session) {
		log.info("buyer request to admin for land");
		// Get buyerId from session
		Long buyerId = (Long) session.getAttribute("buyerId");

		if (buyerId != null) {
			Buyer buyer = buyerService.findById(buyerId); // Fetch buyer by ID
			log.info("buyer: " + buyer);
			landRequest.setBuyer(buyer); // Set the buyer
			buyerService.saveLandRequest(landRequest);
		}
		log.info("landRequest: " + landRequest + " buyerId: " + buyerId);

		try {
			// Store the requested land
			landRequestService.requestLand(landRequest); 
		} catch (IllegalArgumentException iae) {
			log.error("Land request already exists.");
			return EndPoints.BUYER_DASHBOARD_PAGE;
		}

		return EndPoints.BUYER_DASHBOARD_PAGE;
	}

	// admin dashboard
	@GetMapping(EndPoints.ADMIN_DASHBOARD)
	public String getAdminDashboard(Model model) {
		// Fetch the requested lands (this method exists in LandService)
		List<LandRequestDTO> requestedLands = landRequestService
				.getRequestedLands(EndPoints.STATUS_PENDING);

		log.info("requestedLands: " + requestedLands.size());

		// Add the list of requested lands to the model
		model.addAttribute("requestedLands", requestedLands);

		return EndPoints.ADMIN_DASHBOARD_PAGE;
	}

	// view land request
	@GetMapping(EndPoints.BUYER_VIEW_LAND_REQUEST)
	public String viewBuyerLandRequest(HttpSession session, Model model) {
		log.info("view buyer land request");
		// Get buyerId from session
		Long buyerId = (Long) session.getAttribute("buyerId");

		List<LandRequestDTO> requestLandList;
		if (buyerId != null) {
			// Fetch land details for the logged-in buyer
			requestLandList = buyerService.getLandDetailsByBuyerId(buyerId);
			model.addAttribute("requestLandList", requestLandList);
			log.info("Land details for buyerId {}: {}", buyerId, requestLandList);
		} else {
			log.error("Buyer ID not found in session.");
			// Redirect to login page if buyerId is not in session
			return EndPoints.BUYER_DASHBOARD_PAGE;
		}

		return EndPoints.BUYER_LAND_REQUEST_PAGE;
	}

	// view sell land request
	@GetMapping(EndPoints.SELLER_VIEW_LAND_REQUEST)
	public String viewSellerSellLandRequest(HttpSession session, Model model) {
		log.info("view seller land request");

		// Get sellerId from session
		Long sellerId = (Long) session.getAttribute("sellerId");

		List<AddLand> sellLandList;
		if (sellerId != null) {
			// Fetch sell land details for the logged-in seller 
			// with VERIFIED status
			sellLandList = sellerService
					.getRequestLandDetailsBySellerIdAndStatus(sellerId, 
							EndPoints.STATUS_VERIFIED);
			model.addAttribute("sellLandList", sellLandList);
			log.info("Sell land details for sellerId {} with status VERIFIED: {}",
					sellerId, sellLandList);
		} else {
			log.error("Seller ID not found in session.");
			// Redirect to dashboard page if sellerId is not in session
			return EndPoints.SELLER_DASHBOARD_PAGE;
		}

		return EndPoints.SELLER_LAND_REQUEST_PAGE;
	}

	// approved land request
	@ResponseBody
	@PostMapping(EndPoints.ADMIN_APPROVE_ID)
	public ResponseEntity<String> approveLandRequest(@PathVariable Long id) {
		log.info("admin approve request id: " + id);

		try {
			buyerService.approveLandRequest(id);
			// Update the add_land_details status after approval
			buyerService.updateAddLandDetailsStatus(); 
			return ResponseEntity.ok("Land request approved successfully!");
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Error approving land request.");
		}
	}

	// reject land request
	@ResponseBody
	@PostMapping(EndPoints.ADMIN_REJECT_ID)
	public ResponseEntity<String> rejectLandRequest(@PathVariable Long id) {
		log.info("admin reject request id: " + id);

		try {
			// Assuming the buyerService has a rejectLandRequest method
			buyerService.rejectLandRequest(id);
			// Update the add_land_details status after reject
			buyerService.updateAddLandDetailsStatus(); 
			return ResponseEntity.ok("Land request rejected successfully!");
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Error rejecting land request.");
		}
	}

	// Update status for Sell Land Request
	@PostMapping(EndPoints.SELLER_REQUEST_LAND_SELL_PROPERTY_ID)
	public ResponseEntity<String> sellLand(@PathVariable Long propertyId) {
		try {
			log.info("seller sell land propertyId: " + propertyId);

			landRequestService.updateLandStatus(propertyId, 
					EndPoints.STATUS_SUCCESS);
			sellerService.updateAddLandDetailsStatus();
			return ResponseEntity.ok("Land sold successfully.");
		} catch (Exception e) {
			return ResponseEntity.badRequest().body("Error selling land: " 
					+ e.getMessage());
		}
	}

	// Update status for Not Sell Land Request
	@PostMapping(EndPoints.SELLER_REQUEST_LAND_NOT_SELL_PROPERTY_ID)
	public ResponseEntity<String> notSellLand(@PathVariable Long propertyId) {
		try {
			log.info("seller not sell land propertyId: " + propertyId);

			landRequestService.updateLandStatus(propertyId, 
					EndPoints.STATUS_FAILED);
			sellerService.updateAddLandDetailsStatus();
			return ResponseEntity.ok("Land sale request marked as not sold.");
		} catch (Exception e) {
			return ResponseEntity.badRequest()
					.body("Error marking land as not sold: " + e.getMessage());
		}
	}

	// make payment
	@GetMapping(EndPoints.BUYER_MAKE_PAYMENT)
	public String makePayment(HttpSession session, Model model) {
		log.info("make payment page invoked");

		// Get buyerId from session
		Long buyerId = (Long) session.getAttribute("buyerId");

		List<LandRequestDTO> land;
		if (buyerId != null) {
			// Fetch the requested lands (this method exists in LandService)
			land = buyerService.getPaymentLand(buyerId, EndPoints.STATUS_SUCCESS);
			model.addAttribute("land", land);
			log.info("land details landList: " + land);
			return "buyer/buyer-make-payment";
		}
		return EndPoints.BUYER_DASHBOARD_PAGE;
	}

	@PostMapping(EndPoints.BUYER_LAND_TRANSACTION)
	public String submitTransaction(@RequestParam Integer area,
			@RequestParam String city, @RequestParam String state,
			@RequestParam Integer propertyId, @RequestParam Integer surveyNo,
			@RequestParam BigDecimal price,
			HttpSession session, Model model) throws SQLException {

		// Get seller details by propertyId
		Seller seller = sellerService.getSellerDetails(propertyId);

		// Get seller details by propertyId
		Buyer buyer = buyerService.getBuyerDetails(propertyId);

		LandTransaction landTxnDetails = LandTransaction.builder()
				.area(area).city(city).state(state)
				.propertyId(propertyId).physicalSurveyNo(surveyNo)
				.price(price).sellerName(seller.getName())
				.sellerAddress(seller.getSellerAddress())
				.buyerName(buyer.getName())
				.buyerAddress(buyer.getBuyerAddress()).build();

		log.info("landTxnDetails: " + landTxnDetails);

		// Send the JSON data to Node.js server
		String response = transactionService.sendTransactionToNode(landTxnDetails);
		log.info("response: " + response);

		// Check the response from the Node.js server
		if (response == null || response.isEmpty()) {
			model.addAttribute("errorMessage", "Payment failed try again later.");
			return EndPoints.BUYER_TRANSACTION_FAILED_PAGE;
		}

		// Create ObjectMapper to parse JSON response
		ObjectMapper objectMapper = new ObjectMapper();
		try {
			// Convert JSON response to TransactionResponse object
			TransactionResponse transactionResponse = 
					objectMapper.readValue(response, TransactionResponse.class);
			log.info("convert JSON to java object transactionResponse: " 
					+ transactionResponse);

			transactionService.saveLandTransaction(landTxnDetails);
			// Save the response data to the database
			transactionService.saveResponse(transactionResponse);

			// Update the status of the land to 'purchased'
			transactionService.updateLandStatus(propertyId);

			// Add the response to the model to show it in the payment success page
			model.addAttribute("paymentMessage", "Payment success, "
					+ "land purchased.");

			// generate PDF after purchased land
			String ipfsResponse = pdfGenerator.uploadPdfToIpfs(landTxnDetails);

			log.info("IPFS response: " + ipfsResponse);

			// Parse the JSON response to get the IPFS hash
			JSONObject jsonResponse = new JSONObject(ipfsResponse);
			String ipfsHash = jsonResponse.getString("ipfsHash");

			// Add the response to the model
			model.addAttribute("ipfsHash", ipfsHash);

		} catch (DataIntegrityViolationException e) {
			log.error("Duplicate entry error: {}", e.getMessage());
			model.addAttribute("errorMessage", "Invaild transaction "
					+ "this transaction has already been recorded.");
			return EndPoints.BUYER_TRANSACTION_FAILED_PAGE;
		} catch (Exception e) {
			log.error("Error parsing response JSON: {}", e.getMessage());
			model.addAttribute("errorMessage", "Unable to process the payment.");
			return EndPoints.BUYER_TRANSACTION_FAILED_PAGE;
		}

		return EndPoints.BUYER_TRANSACTION_SUCCESS_PAGE;
	}

	// make payment
	@GetMapping(EndPoints.BUYER_PURCHASED_LAND)
	public String purchasedLand(HttpSession session, Model model) {
		log.info("buyer land page invoked");

		// Get buyerId from session
		Long buyerId = (Long) session.getAttribute("buyerId");

		List<LandRequestDTO> land;
		if (buyerId != null) {
			// Fetch the requested lands (this method exists in LandService)
			land = buyerService.getPaymentLand(buyerId, 
					EndPoints.STATUS_PURCHASED);
			model.addAttribute("land", land);
			log.info("land details landList: " + land);

			return EndPoints.BUYER_PURCHASED_LAND_PAGE;
		}

		return EndPoints.BUYER_DASHBOARD_PAGE;
	}

	// get the land document from ipfs
	@GetMapping(EndPoints.BUYER_LAND_FILE_HASH)
	public Mono<ResponseEntity<ByteArrayResource>> 
	getFile(@PathVariable String fileHash) {
		log.info("fileHash: " + fileHash);

		return webClient.get().uri(EndPoints.NODE_API_FILE_HASH, fileHash)
				.retrieve().bodyToMono(byte[].class)
				.map(fileData -> {
					ByteArrayResource resource = new ByteArrayResource(fileData);
					return ResponseEntity.ok().header(HttpHeaders
							.CONTENT_DISPOSITION, "inline; filename=document.pdf")
							.contentType(MediaType.APPLICATION_PDF).body(resource);
				}).onErrorResume(e -> {
					log.error("Error retrieving file from IPFS", e);
					return Mono.just(ResponseEntity
							.status(HttpStatus.INTERNAL_SERVER_ERROR)
							.contentType(MediaType.TEXT_PLAIN)
							.body(new ByteArrayResource(
									"Failed to retrieve document.".getBytes())));
				});
	}
}