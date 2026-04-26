package com.lr.landregistration.pojo;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
	    name = "add_land_details",
	    uniqueConstraints = {
	        @UniqueConstraint(columnNames = "propertyId"),
	        @UniqueConstraint(columnNames = "physicalSurveyNo")
	    }
	)
public class AddLand {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull(message = "Area is required")
    @Column(name = "area", nullable = false)
    private Integer area; // Area of the land in square meters

    @NotBlank(message = "City is required")
    @Column(name = "city", nullable = false)
    private String city; // City where the land is located
    
    @NotBlank(message = "State is required")
    @Column(name = "state", nullable = false)
    private String state; // State where the land is located
    
    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    @Digits(integer = 10, fraction = 2, 
    message = "Price must be a valid amount with at most 10 digits and 2 decimal places")
    @Column(name = "price", nullable = false)
    private BigDecimal price; // Price of the land
    
    @NotNull(message = "Property PID No is required")
    private Integer propertyId; // Property PID number
    
    @NotNull(message = "Physical Survey No is required")
    private Integer physicalSurveyNo; // Physical Survey Number

    @NotNull
    @NotBlank(message = "Aadhar No is required")
    @Size(min = 12, max = 12, message = "Aadhar No must be 12 characters long")
    @Column(name = "aadhar_no", nullable = false)
    private String aadharNo; // Aadhar Number of the seller

    @Lob
    @Column(name = "land_img", nullable = false, columnDefinition = "LONGBLOB")
    private byte[] landImage; // For storing the uploaded landImage document as byte array
    
    @Column(name = "status")
    private String status;

    @ManyToOne
    @JoinColumn(name = "seller_id", nullable = false)
    private Seller seller;  // Foreign key reference
}
