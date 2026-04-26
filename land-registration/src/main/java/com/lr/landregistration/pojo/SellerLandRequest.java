package com.lr.landregistration.pojo;

import java.math.BigDecimal;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Entity
@Table(name = "add_land_details")
public class SellerLandRequest {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotNull(message = "Area is required")
	private Integer area; // Area of the land in square meters

	@NotBlank(message = "City is required")
	private String city; // City where the land is located

	@NotBlank(message = "State is required")
	private String state; // State where the land is located

	@NotNull(message = "Price is required")
	@DecimalMin(value = "0.01", message = "Price must be greater than 0")
	@Digits(integer = 10, fraction = 2, 
	message = "Price must be a valid amount with at most 10 digits and 2 decimal places")
	private BigDecimal price; // Price of the land

	@NotNull(message = "Property PID No is required")
	private Integer propertyId; // Property PID number

	@NotNull(message = "Physical Survey No is required")
	private Integer physicalSurveyNo; // Physical Survey Number

	private String status;
}
