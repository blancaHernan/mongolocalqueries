package com.bhern.mongoqueries.model;

import java.util.Date;
import java.util.List;

/**
 * Ad information including the extended information
 * 
 * @author blancaHN
 *
 */

public class ExtendedAd {
	private long adId;
	private long orgId;
	private String source;
	private int imagesCount;
	private int textLenght;
	private double price;
	private Date adCreated;
	private Date adPublish;
	private Date lastInspection;
	private List<Product> productList;
	private MainCategory mainCategory;
	private String postalCode;
	
	public long getAdId() {
		return adId;
	}

	public String getSource() {
		return source;
	}

	public int getImagesCount() {
		return imagesCount;
	}

	public int getTextLenght() {
		return textLenght;
	}

	public double getPrice() {
		return price;
	}

	public Date getAdCreated() {
		return adCreated;
	}

	public Date getAdPublish() {
		return adPublish;
	}

	public Date getLastInspection() {
		return lastInspection;
	}
	public List<Product> getProductList() {
		return productList;
	}
	

	public MainCategory getMainCategory() {
		return mainCategory;
	}

	public String getPostalCode() {
		return postalCode;
	}
	

	public long getOrgId() {
		return orgId;
	}

	public ExtendedAd(long orgId, long adId, String source, int imagesCount, int textLenght, double price, Date adCreated,
			Date adPublish, Date lastInspection, List<Product> productList, MainCategory mainCategory, String postalCode) {
		super();
		this.orgId = orgId;
		this.adId = adId;
		this.source = source;
		this.imagesCount = imagesCount;
		this.textLenght = textLenght;
		this.price = price;
		this.adCreated = adCreated;
		this.adPublish = adPublish;
		this.lastInspection = lastInspection;
		this.productList = productList;
		this.mainCategory = mainCategory;
		this.postalCode = postalCode;
	}

	
}
