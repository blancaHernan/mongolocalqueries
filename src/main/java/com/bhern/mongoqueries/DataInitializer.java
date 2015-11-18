package com.bhern.mongoqueries;

import java.util.ArrayList;
import java.util.List;

import com.bhern.mongoqueries.model.MainCategory;
import com.bhern.mongoqueries.model.PriceRange;

public class DataInitializer {
	private static Integer[] categoryList = {8205, 73, 69, 68, 8216, 8214, 77, 76, 8215, 8206, 
			8212, 71, 8210, 8207, 8209, 8201, 83, 8213, 8208};
	private static String[] categoryNameList = {"Dienstleistungen", 
			"Handy  Organizer  Telefon", 
			"Sport  Sportgeräte", 
			"Baby  Kind", 
			"Foto  TV  Video  Audio", 
			"Haushalt Küchengeräte  Gastronomie", 
			"Tiere  Zubehör", 
			"Antiquitäten  Sammlungen  Kunst", 
			"PC-Spiele   Videospiele", 
			"Gesundheit kosmetik wellness", 
			"Spielwelt", 
			"Kleidung Accessoires", 
			"Heimwerken Garten", 
			"Kfz Zubehoer motorradteile", 
			"Buecher Filme Musik", 
			"Moebel Wohnen Buero", 
			"Freizeit Hobby Kulinarik", 
			"Uhren Schmuck", 
			"PC hardware software"};
	
	public static List<MainCategory> init(){
		List<MainCategory> categories = new ArrayList();
		boolean equalSize = categoryList.length == categoryNameList.length;
		if(!equalSize){
			System.out.println("WARNING!! Some errors in the initialiazation!!");
		}
		for(int i = 0; i < categoryList.length; i++){
			categories.add(new MainCategory(categoryNameList[i], 
					categoryList[i], getPrices(categoryList[i])));
		}
		return categories;
	}
	
	private static List<PriceRange> getPrices(int attributeId){
		//TODO: price ranges from Reinhard
		List<PriceRange> priceRanges = new ArrayList<PriceRange>();
		priceRanges.add(new PriceRange(0, 1));
		priceRanges.add(new PriceRange(1, 10));
		priceRanges.add(new PriceRange(11, 50));
		priceRanges.add(new PriceRange(51, 80));
		priceRanges.add(new PriceRange(0, 1));
		priceRanges.add(new PriceRange(81, 99));
		priceRanges.add(new PriceRange(100, 200));
		priceRanges.add(new PriceRange(201, 350));
		priceRanges.add(new PriceRange(351, 500));
		priceRanges.add(new PriceRange(501, 1000));
		priceRanges.add(new PriceRange(1000, Integer.MAX_VALUE));
		
		return priceRanges;
	}
}
