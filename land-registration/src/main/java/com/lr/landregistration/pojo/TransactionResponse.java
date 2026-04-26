package com.lr.landregistration.pojo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Entity
@Table(name = "transaction_details")
public class TransactionResponse {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotBlank(message = "PaymentHash is required")
	@Column(name = "payment_hash", nullable = false, unique = true)
	private String paymentHash;

	@NotBlank(message = "IpfsHash is required")
	@Column(name = "ipfs_hash", nullable = false, unique = true)
	private String ipfsHash;

	@NotBlank(message = "message is required")
	@Column(name = "status", nullable = false)
	private String message;
}
