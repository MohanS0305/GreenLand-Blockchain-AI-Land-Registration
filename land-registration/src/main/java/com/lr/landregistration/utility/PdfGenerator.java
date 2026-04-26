package com.lr.landregistration.utility;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.DottedLineSeparator;
import com.itextpdf.text.pdf.draw.LineSeparator;
import com.lr.landregistration.pojo.LandTransaction;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PdfGenerator {
	private static Font COURIER_BOLD = 
			new Font(Font.FontFamily.COURIER, 20, Font.BOLD);
	private static Font COURIER_SMALL_BOLD_ITALIC = 
			new Font(Font.FontFamily.COURIER, 14, Font.BOLDITALIC);
	private static Font COURIER_SMALL_FOOTER = 
			new Font(Font.FontFamily.COURIER, 10, Font.NORMAL);
	private static Font COURIER_NORMAL = 
			new Font(Font.FontFamily.COURIER, 12, Font.NORMAL);

	@Autowired
	private RestTemplate restTemplate;

	@Value("${pdfDir}")
	private String pdfDir;

	@Value("${reportFileName}")
	private String reportFileName;

	@Value("${reportFileNameDateFormat}")
	private String reportFileNameDateFormat;

	@Value("${localDateFormat}")
	private String localDateFormat;

	@Value("${title}")
	private String title;

	@Value("${subTitle}")
	private String subTitle;

	@Value("${footer}")
	private String footer;

	public ByteArrayOutputStream generatePdfReport(LandTransaction txnDetails) 
			throws IOException {
		log.info("======== Generating pdf ========");
		log.info("txnDetails = {}", txnDetails);

		Document document = new Document();
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		try {
			System.out.println("Working Directory = " 
					+ System.getProperty("user.dir"));

			// Create the directory if it doesn't exist
			File dir = new File(pdfDir);
			if (!dir.exists()) {
				log.info("Creating directory: " + dir.mkdirs());
				dir.mkdirs(); // Create the directory
			}

			PdfWriter.getInstance(document, byteArrayOutputStream);
			document.open();

			// add title to document
			addDocTitle(document);

			// add content to document
			addDocContent(txnDetails, document);

			// add footer to document
			addFooter(document);

			document.close();
			log.info("------------------Your PDF Report is ready!"
					+ "-------------------------");

		} catch (DocumentException de) {
			log.error("Error generating PDF: {}", de.getMessage());
		}

		return byteArrayOutputStream;
	}

	// Document title
	private void addDocTitle(Document document) throws DocumentException {
		log.info("add title to pdf");
		log.info("title: " + title);
		Paragraph header = new Paragraph(title, COURIER_BOLD);
		header.setAlignment(Element.ALIGN_CENTER);
		document.add(header);
		document.add(addSpace(2));

		LineSeparator ls = new LineSeparator();
		document.add(new Chunk(ls));
	}

	// Document main content
	private void addDocContent(LandTransaction txnDetails, 
			Document document) throws DocumentException {
		log.info("add content to pdf");

		PdfPTable table = new PdfPTable(2); // 2 columns
		table.setWidthPercentage(100); // Make the table span the width of the document
		table.setSpacingBefore(10f); // space before the table

		// Create the Buyer Name cell
		PdfPCell buyerNameCell = new PdfPCell(new Phrase("Buyer Name: " 
				+ txnDetails.getBuyerName(), COURIER_NORMAL));
		buyerNameCell.setBorder(Rectangle.NO_BORDER); // No border for a clean look
		buyerNameCell.setHorizontalAlignment(Element.ALIGN_LEFT); // Align left
		buyerNameCell.setVerticalAlignment(Element.ALIGN_MIDDLE); // Align middle
		table.addCell(buyerNameCell);

		// Create the Seller Name cell
		PdfPCell sellerNameCell = new PdfPCell(
				new Phrase("Seller Name: " + txnDetails.getSellerName(), 
						COURIER_NORMAL));
		sellerNameCell.setBorder(Rectangle.NO_BORDER); // No border for a clean look
		sellerNameCell.setHorizontalAlignment(Element.ALIGN_RIGHT); // Align right
		sellerNameCell.setVerticalAlignment(Element.ALIGN_MIDDLE); // Align middle
		table.addCell(sellerNameCell);

		// Create the Buyer addresses
		PdfPCell buyerAddressCell = new PdfPCell(
				new Phrase("Buyer Address: " + maskAddress(txnDetails
						.getBuyerAddress()), COURIER_NORMAL));
		buyerAddressCell.setBorder(Rectangle.NO_BORDER); // No border for a clean look
		buyerAddressCell.setHorizontalAlignment(Element.ALIGN_LEFT); // Align left
		buyerAddressCell.setVerticalAlignment(Element.ALIGN_MIDDLE); // Align middle
		table.addCell(buyerAddressCell);

		// Create the Seller Address cell
		PdfPCell sellerAddressCell = new PdfPCell(
				new Phrase("Seller Address: " + maskAddress(txnDetails
						.getSellerAddress()), COURIER_NORMAL));
		sellerAddressCell.setBorder(Rectangle.NO_BORDER); // No border for a clean look
		sellerAddressCell.setHorizontalAlignment(Element.ALIGN_RIGHT); // Align right
		sellerAddressCell.setVerticalAlignment(Element.ALIGN_MIDDLE); // Align middle
		table.addCell(sellerAddressCell);

		// Add the table to the document
		document.add(table);
		document.add(addSpace(2)); // Add space after the table

		// Document sub title
		Paragraph p1 = new Paragraph();
		leaveEmptyLine(p1, 1);
		p1.add(new Paragraph(subTitle, COURIER_SMALL_BOLD_ITALIC));
		p1.setAlignment(Element.ALIGN_LEFT);
		p1.add(new Chunk(new DottedLineSeparator()));

		// add p1 to document
		document.add(p1);

		// land details
		Paragraph p2 = new Paragraph();
		leaveEmptyLine(p2, 1);
		// land owner name
		p2.add(new Paragraph("Land Owner Name: " 
				+ txnDetails.getBuyerName(), COURIER_NORMAL));
		leaveEmptyLine(p2, 1);

		// land size(area)
		p2.add(new Paragraph("Land Size(area): " 
				+ txnDetails.getArea() + "sqt", COURIER_NORMAL));
		leaveEmptyLine(p2, 1);

		// city for land
		p2.add(new Paragraph("City For Land: " 
				+ txnDetails.getCity(), COURIER_NORMAL));
		leaveEmptyLine(p2, 1);

		// state for land
		p2.add(new Paragraph("State For Land: " 
				+ txnDetails.getState(), COURIER_NORMAL));
		leaveEmptyLine(p2, 1);

		// landId(propertyId) for land
		p2.add(new Paragraph("Land Id(propertyId): " 
				+ txnDetails.getPropertyId(), COURIER_NORMAL));
		leaveEmptyLine(p2, 1);

		// physicalSurveyNo
		p2.add(new Paragraph("PhysicalSurveyNo For Land: " 
				+ txnDetails.getPhysicalSurveyNo(), COURIER_NORMAL));
		leaveEmptyLine(p2, 1);

		// price for land
		p2.add(new Paragraph("Price(INR) For Land: " 
				+ txnDetails.getPrice(), COURIER_NORMAL));
		leaveEmptyLine(p2, 1);
		leaveEmptyLine(p2, 1);

		// add p2 in document
		document.add(p2);
	}

	// Document footer content
	private void addFooter(Document document) throws DocumentException {
		log.info("add footer to pdf");
		log.info("footer: " + footer);
		String localDateString = LocalDateTime.now()
				.format(DateTimeFormatter.ofPattern(localDateFormat));
		Paragraph footerParagraph = new Paragraph(footer, 
				COURIER_SMALL_BOLD_ITALIC);
		footerParagraph.setAlignment(Element.ALIGN_CENTER);
		footerParagraph.add(new Paragraph("Land document generated on " 
				+ localDateString, COURIER_SMALL_FOOTER));
		document.add(footerParagraph);
	}

	// add some space
	private static Paragraph addSpace(int size) {
		Font LineBreak = FontFactory.getFont("Arial", size);
		Paragraph paragraph = new Paragraph("\n\n", LineBreak);
		return paragraph;
	}

	// getCell
	public PdfPCell getCell(String text, int alignment) {
		PdfPCell cell = new PdfPCell(new Phrase(text));
		cell.setPadding(0);
		cell.setHorizontalAlignment(alignment);
		cell.setBorder(PdfPCell.NO_BORDER);
		return cell;
	}

	// leave empty line
	private static void leaveEmptyLine(Paragraph paragraph, int number) {
		for (int i = 0; i < number; i++) {
			paragraph.add(new Paragraph(" "));
		}
	}

	// Helper function to mask the address
	private String maskAddress(String address) {
		// Check if the address is long enough
		if (address.length() > 6) {
			// Return the first six characters followed by "xxxx"
			return address.substring(0, 6) + "XXXX";
		} else {
			// If the address is less than or equal to 6 characters
			return address;
		}
	}

	// PDF name with date
	private String getPdfNameWithDate(LandTransaction txnDetails) {
		String dateFormat = LocalDateTime.now()
				.format(DateTimeFormatter.ofPattern(reportFileNameDateFormat));
		return pdfDir + txnDetails.getBuyerName() 
		+ reportFileName + dateFormat + ".pdf";
	}

	private String uploadToIpfs(byte[] fileData, String fileName) 
			throws IOException {
		log.info("Uploading file to IPFS....");
		log.info("fileData length: " + fileData.length); // Log the length of the byte array
		log.info("fileName: " + fileName);

		// Define the target URL for the RESTful service
		String targetUrl = "http://localhost:3000/file"; // Change to the actual URL

		// Create headers for the multipart request
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.MULTIPART_FORM_DATA);

		// Create the request body as a MultiValueMap
		MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

		// Wrap byte array in ByteArrayResource
		ByteArrayResource byteArrayResource = new ByteArrayResource(fileData) {
			@Override
			public String getFilename() {
				return fileName; // Specify the filename
			}
		};

		// Add the file data and file name to the request body
		body.add("fileData", byteArrayResource);
		body.add("fileName", fileName); // Optionally send the filename as well

		// Create the request entity
		HttpEntity<MultiValueMap<String, Object>> requestEntity = 
				new HttpEntity<>(body, headers);

		try {
			// Make a POST request to the target URL
			ResponseEntity<String> responseEntity = restTemplate.exchange(
					targetUrl, HttpMethod.POST, requestEntity,
					String.class);
			return responseEntity.getBody(); // Return response body if needed
		} catch (HttpClientErrorException | HttpServerErrorException e) {
			log.error("Error occurred while uploading to IPFS: {}", 
					e.getStatusCode());
			throw new IOException("Error uploading to IPFS: " 
					+ e.getMessage(), e);
		} catch (RestClientException e) {
			log.error("RestClientException occurred: {}", e.getMessage());
			throw new IOException("Error during REST call: " + e.getMessage(), e);
		}
	}

	// UploadPdf to IPFS
	public String uploadPdfToIpfs(LandTransaction txnDetails) {
		try {
			ByteArrayOutputStream byteArrayOutputStream = 
					generatePdfReport(txnDetails);
			byte[] pdfBytes = byteArrayOutputStream.toByteArray();

			// Log the PDF byte array length
			log.info("PDF byte array length: " + pdfBytes.length);

			// Check if the byte array is empty
			if (pdfBytes.length == 0) {
				throw new IOException("PDF byte array is empty.");
			}

			// Call uploadToIpfs method
			return uploadToIpfs(pdfBytes, getPdfNameWithDate(txnDetails));
		} catch (FileNotFoundException e) {
			log.error("File not found: {}", e.getMessage());
		} catch (IOException e) {
			log.error("Error uploading PDF to IPFS", e);
			return null; // Handle accordingly
		}
		return null;
	}
}
