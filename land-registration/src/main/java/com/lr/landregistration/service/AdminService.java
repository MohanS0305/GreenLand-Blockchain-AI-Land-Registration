package com.lr.landregistration.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.lr.landregistration.pojo.Admin;
import com.lr.landregistration.repository.AdminRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class AdminService {

	private AdminRepository adminRepository;

	public AdminService(AdminRepository adminRepository) {
		super();
		this.adminRepository = adminRepository;
	}

	// validate admin login
	public boolean validateAdmin(Admin admin) {
		Optional<Admin> foundAdmin = adminRepository.findByEmail(admin.getEmail());
		log.info("admin: " + foundAdmin);
		// Check if Admin exists and passwords match
		// if it's match Redirect on success otherwise Return on failed
		return foundAdmin.isPresent() && admin.getPassword()
				.equals(foundAdmin.get().getPassword());
	}
}
