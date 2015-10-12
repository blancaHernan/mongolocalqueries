package com.bhern.mongoqueries.model;

import java.util.List;

public class MainCateogry {
	private String name;
	private int valueId;
	private List<PriceRange> priceRange;
	public MainCateogry(String name, int valueId, List<PriceRange> priceRange) {
		super();
		this.name = name;
		this.valueId = valueId;
		this.priceRange = priceRange;
	}
	public String getName() {
		return name;
	}
	public int getValueId() {
		return valueId;
	}
	public List<PriceRange> getPriceRange() {
		return priceRange;
	}
	
	
}
