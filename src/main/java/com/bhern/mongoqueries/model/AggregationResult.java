package com.bhern.mongoqueries.model;

import java.util.ArrayList;
import java.util.List;

public class AggregationResult {
	private long adId;
	private List<String> source;
	public AggregationResult(long adId, List<String> sources) {
		super();
		this.adId = adId;
		this.source = sources;
	}
	public long getAdId() {
		return adId;
	}
	public List<String> getSource() {
		return source;
	}
	
}
