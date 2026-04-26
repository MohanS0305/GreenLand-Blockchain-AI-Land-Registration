package com.lr.landregistration.pojo;

import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "seller_registration")
public class Seller {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", nullable = false)
    private Long id;

    @NotBlank(message = "Name is required")
    @Column(name = "name", nullable = false)
    private String name;

    @NotNull(message = "Age is required")
    @Column(name = "age", nullable = false)
    private Integer age;

    @NotBlank(message = "City is required")
    @Column(name = "city", nullable = false)
    private String city;

    @NotNull
    @NotBlank(message = "Aadhar No is required")
    @Size(min = 12, max = 12, message = "Aadhar No must be 12 characters long")
    @Column(name = "aadhar_no", nullable = false, unique = true)
    private String aadharNo;

    @NotNull
    @NotBlank(message = "PAN No is required")
    @Size(min = 10, max = 10, message = "PAN No must be 10 characters long")
    @Column(name = "pan_no", nullable = false, unique = true)
    private String panNo;

    @Lob
    @Column(name = "id_proof", nullable = false, columnDefinition = "LONGBLOB")
    private byte[] document;  // For storing the uploaded document as byte array
    
    @NotBlank(message = "Seller Block Chain Address is required")
    @Column(name = "seller_address", nullable = false, unique = true)
    private String sellerAddress;

    @Email(message = "Email should be valid")
    @NotBlank(message = "Email is required")
    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @NotBlank(message = "Password is required")
    @Column(name = "password", nullable = false)
    private String password;
    
    @ToString.Exclude
    @OneToMany(mappedBy = "seller", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AddLand> landDetails;
}
