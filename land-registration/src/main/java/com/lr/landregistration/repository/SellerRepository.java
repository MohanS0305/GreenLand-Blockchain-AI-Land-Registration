package com.lr.landregistration.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.lr.landregistration.pojo.Seller;

public interface SellerRepository extends JpaRepository<Seller, Long> {
	Optional<Seller> findByEmail(String email);

	// Query to count the number of rows in the buyer_registration table
    @Query("SELECT COUNT(s) FROM Seller s")
    Long countAllSellers();
	
	// Custom query to only fetch seller ID by email
	@Query("SELECT s.id FROM Seller s WHERE s.email = :email")
	Optional<Long> findSellerIdByEmail(@Param("email") String email);

	Optional<Seller> findByAadharNo(String aadharNo);
}
