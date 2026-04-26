package com.lr.landregistration.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.client.RestTemplate;

import com.lr.landregistration.utility.PdfGenerator;

@Configuration
public class AppConfig {

	// RestTemplate Object
	@Bean
	RestTemplate newRestTemplate() {
		return new RestTemplate();
	}
	
	// HttpHeaders Object
	@Bean
	HttpHeaders newHttpHeaders() {
		return new HttpHeaders();
	}
	
	// BCryptPasswordEncoder Object
	@Bean
	BCryptPasswordEncoder newBCryptPasswordEncoder() {
		return new BCryptPasswordEncoder();
	}
	
	// PdfGenerator Object
	@Bean
    PdfGenerator pdfGenerator() {
        return new PdfGenerator();
    }

}
