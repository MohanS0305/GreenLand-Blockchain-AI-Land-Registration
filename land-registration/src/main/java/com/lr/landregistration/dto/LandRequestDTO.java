package com.lr.landregistration.dto;

import java.math.BigDecimal;

import com.lr.landregistration.pojo.Buyer;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "land_request_details")
public class LandRequestDTO {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "area", nullable = false)
	private String area;
	@Column(name = "city", nullable = false)
	private String city;
	@Column(name = "state", nullable = false)
	private String state;
	@Column(name = "price", nullable = false)
	private BigDecimal price;
	@Column(name = "property_id", nullable = false)
	private Integer propertyId;
	@Column(name = "survey_no", nullable = false)
	private Integer surveyNo;
	@Column(name = "status", nullable = false)
	private String status;

	@ManyToOne
	@JoinColumn(name = "buyer_id", nullable = false)
	private Buyer buyer;
}
