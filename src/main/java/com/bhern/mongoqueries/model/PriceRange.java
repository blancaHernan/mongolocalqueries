package com.bhern.mongoqueries.model;

public class PriceRange {

	private int minPrice;
	private int maxPrice;
	public PriceRange(int minPrice, int maxPrice) {
		super();
		this.minPrice = minPrice;
		this.maxPrice = maxPrice;
	}
	public int getMinPrice() {
		return minPrice;
	}
	public int getMaxPrice() {
		return maxPrice;
	}
	
	
}
