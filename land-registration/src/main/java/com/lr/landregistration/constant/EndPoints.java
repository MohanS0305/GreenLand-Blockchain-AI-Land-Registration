package com.lr.landregistration.constant;

public class EndPoints {
	
	public static final String LANDREGISTER = "/land-register";
	public static final String EMPTY_SLASH = "/";
	public static final String ADMIN_LOGIN = "/admin/login";
	public static final String ADMIN_DASHBOARD = "/admin/dashboard";
	public static final String BUYER_LOGIN = "/buyer/login";
	public static final String BUYER_REGISTRATION = "/buyer/register";
	public static final String BUYER_DASHBOARD = "/buyer/dashboard";
	public static final String SELLER_LOGIN = "/seller/login";
	public static final String SELLER_REGISTRATION = "/seller/register";
	public static final String SELLER_DASHBOARD = "/seller/dashboard";
	public static final String SELLER_ADD_LAND = "/seller/add-land";
	public static final String ADMIN_APPROVE_ID = "/admin/approve/{id}";
	public static final String ADMIN_REJECT_ID = "/admin/reject/{id}";
	public static final String BUYER_REQUEST_LAND = "/buyer/request-land";
	public static final String BUYER_VIEW_LAND_REQUEST = "/buyer/view-land-request";
	public static final String SELLER_VIEW_LAND_REQUEST = "/seller/view-sell-land-request";
	public static final String SELLER_REQUEST_LAND_NOT_SELL_PROPERTY_ID = "seller/request-land/not-sell/{propertyId}";
	public static final String SELLER_REQUEST_LAND_SELL_PROPERTY_ID = "seller/request-land/sell/{propertyId}";
	public static final String BUYER_MAKE_PAYMENT = "/buyer/make-payment";
	public static final String BUYER_LAND_TRANSACTION = "/buyer/land-transaction";
	public static final String BUYER_PURCHASED_LAND = "/buyer/purchased-land";
	public static final String BUYER_LAND_FILE_HASH = "/buyer-land/files/{fileHash}";
	public static final String NODE_API_FILE_HASH = "/api/ipfs/{fileHash}";
	
	// File path for HTML page
	public static final String INDEX_PAGE = "index";
	public static final String ADMIN_LOGIN_PAGE = "admin/admin-login";
	public static final String ADMIN_DASHBOARD_PAGE = "admin/admin-dashboard";
	public static final String BUYER_LOGIN_PAGE = "buyer/buyer-login";
	public static final String BUYER_REGISTRATION_PAGE = "buyer/buyer-registration";
	public static final String BUYER_DASHBOARD_PAGE = "buyer/buyer-dashboard";
	public static final String SELLER_LOGIN_PAGE = "seller/seller-login";
	public static final String SELLER_REGISTRATION_PAGE = "seller/seller-registration";
	public static final String SELLER_DASHBOARD_PAGE = "seller/seller-dashboard";
	public static final String SELLER_ADD_LAND_PAGE = "seller/add-land";
	public static final String BUYER_LAND_REQUEST_PAGE = "buyer/buyer-land-request";
	public static final String SELLER_LAND_REQUEST_PAGE = "seller/sell-land-request";
	public static final String BUYER_TRANSACTION_SUCCESS_PAGE = "buyer/buyer-transaction-success";
	public static final String BUYER_TRANSACTION_FAILED_PAGE = "buyer/buyer-transaction-failed";
	public static final String BUYER_PURCHASED_LAND_PAGE = "buyer/buyer-purchase-land";
	
	// Status for land request
	public static final String STATUS_INITIATED = "INITIATED";
	public static final String STATUS_PENDING = "PENDING";
	public static final String STATUS_VERIFIED = "VERIFIED";
	public static final String STATUS_REJECTED = "REJECTED";
	public static final String STATUS_SUCCESS = "SUCCESS";
	public static final String STATUS_FAILED = "FAILED";
	public static final String STATUS_PURCHASED = "PURCHASED";
	
	// To prevent the class from being instantiated
	private EndPoints() {
	}
}
