package com.lr.landregistration.pojo;

import java.math.BigDecimal;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(
	    name = "land_transaction",
	    uniqueConstraints = {
	        @UniqueConstraint(columnNames = "propertyId"),
	        @UniqueConstraint(columnNames = "physicalSurveyNo")
	    }
	)
public class LandTransaction {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY) // Automatically generate the ID
	private Integer id;

	@NotNull(message = "Area is required")
	private Integer area;

	@NotBlank(message = "City is required")
	private String city;

	@NotBlank(message = "State is required")
	private String state;

	@NotNull(message = "Property PID No is required")
	private Integer propertyId;

	@NotNull(message = "Physical Survey No is required")
	private Integer physicalSurveyNo;

	@NotNull(message = "Price is required")
	private BigDecimal price;

	@NotBlank(message = "Seller Name is required")
	private String sellerName;

	@NotBlank(message = "Seller Address is required")
	private String sellerAddress;

	@NotBlank(message = "Buyer Name is required")
	private String buyerName;

	@NotBlank(message = "Buyer Address is required")
	private String buyerAddress;
}
