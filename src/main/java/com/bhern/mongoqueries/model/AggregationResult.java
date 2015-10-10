package com.bhern.mongoqueries.model;


public class AggregationResult {
	private Long adId;
	private Object source;
	public AggregationResult(Long adId, Object source) {
		this.adId = adId;
		this.source = source;
	}
	public Long getAdId() {
		return adId;
	}
	public Object getSource() {
		return source;
	}
	
}
