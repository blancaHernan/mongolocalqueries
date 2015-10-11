package com.bhern.mongoqueries.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AggregationResult {
	private long adId;
	private List<String> source;
	private List<Date> inspectionDate;
	public AggregationResult(Long adId, List<String> sources, List<Date> inspectionDate) {
		super();
		this.adId = adId;
		this.source = sources;
		this.inspectionDate = inspectionDate;
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
	
	
}
