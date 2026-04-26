package com.lr.landregistration.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.lr.landregistration.pojo.Buyer;

public interface BuyerRepository extends JpaRepository<Buyer, Long> {
	// find the buyer by email
	Optional<Buyer> findByEmail(String email);

	// Query to count the number of rows in the buyer_registration table
	@Query("SELECT COUNT(b) FROM Buyer b")
	Long countAllBuyers();

	// Custom query to only fetch buyer ID by email
	@Query("SELECT b.id FROM Buyer b WHERE b.email = :email")
	Optional<Long> findBuyerIdByEmail(@Param("email") String email);
}