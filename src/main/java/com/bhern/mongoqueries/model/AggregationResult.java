package com.bhern.mongoqueries.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AggregationResult {
	private long adId;
	private List<String> source;
	private List<Date> inspectionDate;
	private int count;
	private int imagesCount;
	private int textLenght;
	private double price;
	
	
	
	public AggregationResult(long adId, List<String> source, List<Date> inspectionDate, int count, int imagesCount,
			int textLenght, double price) {
		super();
		this.adId = adId;
		this.source = source;
		this.inspectionDate = inspectionDate;
		this.count = count;
		this.imagesCount = imagesCount;
		this.textLenght = textLenght;
		this.price = price;
	}
	public long getAdId() {
		return adId;
	}
	public List<String> getSource() {
		return source;
	}
	public List<Date> getInspectionDate() {
		return inspectionDate;
	}
	public int getCount() {
		return count;
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
	
	
}
