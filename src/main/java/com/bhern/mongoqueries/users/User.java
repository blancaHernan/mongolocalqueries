package com.bhern.mongoqueries.users;

import java.util.ArrayList;
import java.util.List;

import utils.ListUtils;

public class User {
	private long orgId;
	private int numberAds;
	private int numberOfCategories;
	private int numberOfActiveAds;
	private int numberOfPendingAds;
	private int numberOfSources;
	private List<Long> adIds;
	private List<Integer> mainCategoryIds = new ArrayList();
	private List<String> sources = new ArrayList();
	
	
	public User(long orgId, List<Long> adIds, List<Integer> mainCategoryIds, List<String> adStatus, 
			List<String> sources) {
		super();
		this.orgId = orgId;
		this.adIds = adIds;		
		refineLists(mainCategoryIds, adStatus, sources);
		this.numberOfSources = ListUtils.getListSize(this.sources);
		this.numberAds = ListUtils.getListSize(this.adIds);
		this.numberOfCategories = ListUtils.getListSize(this.mainCategoryIds);
	}
	
	private void refineLists(List<Integer> mainCategoryIds, List<String> adStatus, List<String> sources) {
		for(String status : adStatus){
			if(status.equalsIgnoreCase("ACTIVE")){
				this.numberOfActiveAds++;
			} else if(status.equalsIgnoreCase("PENDING")){
				this.numberOfPendingAds++;
			}
		}
		for(String source : sources){
			if(!this.sources.contains(source)){
				this.sources.add(source);
			}
		}
		for(Integer source : mainCategoryIds){
			if(!this.mainCategoryIds.contains(source)){
				this.mainCategoryIds.add(source);
			}
		}
	}
	public long getOrgId() {
		return orgId;
	}
	public int getNumberAds() {
		return numberAds;
	}
	public int getNumberOfCategories() {
		return numberOfCategories;
	}
	public int getNumberOfActiveAds() {
		return numberOfActiveAds;
	}
	public int getNumberOfPendingAds() {
		return numberOfPendingAds;
	}
	public List<Long> getAdIds() {
		return adIds;
	}
	public List<Integer> getMainCategoryIds() {
		return mainCategoryIds;
	}
	public List<String> getSources(){
		return sources;
	}
	public int getNumberOfSources(){
		return numberOfSources;
	}
}
