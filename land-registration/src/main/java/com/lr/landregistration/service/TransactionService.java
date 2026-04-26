package com.lr.landregistration.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lr.landregistration.pojo.LandTransaction;
import com.lr.landregistration.pojo.TransactionResponse;
import com.lr.landregistration.repository.AddLandRepository;
import com.lr.landregistration.repository.LandRequestRepository;
import com.lr.landregistration.repository.LandTransactionRepository;
import com.lr.landregistration.repository.TransactionRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class TransactionService {

	private RestTemplate restTemplate;
	private HttpHeaders headers;
	private TransactionRepository transactionRepository;
	private LandRequestRepository landRequestRepository;
	private AddLandRepository addLandRepository;
	@Autowired
	private LandTransactionRepository landTransactionRepository;

	public TransactionService(RestTemplate restTemplate, HttpHeaders headers,
			TransactionRepository transactionRepository, 
			LandRequestRepository landRequestRepository,
			AddLandRepository addLandRepository) {
		super();
		this.restTemplate = restTemplate;
		this.headers = headers;
		this.transactionRepository = transactionRepository;
		this.landRequestRepository = landRequestRepository;
		this.addLandRepository = addLandRepository;
	}

	public String sendTransactionToNode(LandTransaction transactionData) {
		log.info("transactionData: " + transactionData);

		// Node.js backend URL that handles the IPFS and blockchain transaction
		String nodeJsUrl = "http://localhost:3000/make-transaction";

		// Set headers
		headers.set("Content-Type", "application/json");

		// Convert the transactionData Java object to JSON
		ObjectMapper objectMapper = new ObjectMapper();
		String requestBody;

		try {
			requestBody = objectMapper.writeValueAsString(transactionData); // Convert to JSON
			log.info("requestBody: " + requestBody);
		} catch (Exception e) {
			e.printStackTrace();
			return "Error occurred while converting transaction data to JSON: " 
			+ e.getMessage();
		}

		// Create the HTTP entity containing headers and body
		HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);

		try {
			log.info("sending request to node.js backend");

			// Send POST request to the Node.js backend
			ResponseEntity<String> response = 
					restTemplate.exchange(nodeJsUrl, HttpMethod.POST, 
							requestEntity, String.class);

			// Return the response from Node.js 
			// (should contain IPFS hash and transaction details)
			return response.getBody();
		} catch (Exception e) {
			e.printStackTrace();
			return "Error occurred while sending data to Node.js: " 
			+ e.getMessage();
		}
	}

	// save transaction response in database
	public TransactionResponse saveResponse(TransactionResponse response) {
		log.info("Saving transaction...");
		return transactionRepository.save(response);
	}

	// paid land update status to purchased
	public void updateLandStatus(Integer propertyId) {
		landRequestRepository.updateLandStatusToPurchased(propertyId);
		addLandRepository.updateLandStatusToPurchased(propertyId);
	}

	// save LandTransaction in DB
	public void saveLandTransaction(LandTransaction landTxnDetails) {
		landTransactionRepository.save(landTxnDetails);
		log.info("LandTransaction stored successfully.");
	}

}