package com.bhern.mongoqueries.model;

/**
 * Products that can be bought for the ads
 * 
 * @author blancaHN
 *
 */
public class Product {
	private long featureId;
	private String productType;
	private String productName;
	private double price;
	
	public Product(long featureId, String productType, String productName, double price) {
		super();
		this.featureId = featureId;
		this.productType = productType;
		this.productName = productName;
		this.price = price;
	}
	
	public long getFeatureId() {
		return featureId;
	}
	public String getProductType() {
		return productType;
	}
	public String getProductName() {
		return productName;
	}
	public double getPrice() {
		return price;
	}
	
	
}
