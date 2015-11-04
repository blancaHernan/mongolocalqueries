package com.bhern.mongoqueries;

import java.util.ArrayList;
import java.util.List;

import com.bhern.mongoqueries.model.MainCategory;
import com.bhern.mongoqueries.model.PriceRange;

public class DataInitializer {
	private static Integer[] categoryList = {8205, 73, 69, 68, 8216, 8214, 77, 76, 8215};
	private static String[] categoryNameList = {"Dienstleistungen", 
			"Handy  Organizer  Telefon", 
			"Sport  Sportgeräte", 
			"Baby  Kind", 
			"Foto  TV  Video  Audio", 
			"Haushalt Küchengeräte  Gastronomie", 
			"Tiere  Zubehör", 
			"Antiquitäten  Sammlungen  Kunst", 
			"PC-Spiele   Videospiele"};
	
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
		switch(attributeId){
			case 8205:
				priceRanges.add(new PriceRange(0, 19));
				priceRanges.add(new PriceRange(20, 359));
				priceRanges.add(new PriceRange(360, 1499));
				priceRanges.add(new PriceRange(1500, 5999));
				priceRanges.add(new PriceRange(6000, 80000));
				break;
			case 73:
				priceRanges.add(new PriceRange(0, 9));
				priceRanges.add(new PriceRange(10, 479));
				priceRanges.add(new PriceRange(480, 2499));
				priceRanges.add(new PriceRange(3500, 35000));
				break;
			case 69:
				priceRanges.add(new PriceRange(0, 19));
				priceRanges.add(new PriceRange(20, 459));
				priceRanges.add(new PriceRange(460, 2499));
				priceRanges.add(new PriceRange(2500, 400000));
				break;
			case 68:
				priceRanges.add(new PriceRange(0, 9));
				priceRanges.add(new PriceRange(10, 459));
				priceRanges.add(new PriceRange(460, 5999));
				priceRanges.add(new PriceRange(10000, 60000));
				break;
			case 8216:
				priceRanges.add(new PriceRange(0, 9));
				priceRanges.add(new PriceRange(10, 459));
				priceRanges.add(new PriceRange(460, 2499));
				priceRanges.add(new PriceRange(2500, 400000));
				break;
			case 8214:
				priceRanges.add(new PriceRange(0, 9));
				priceRanges.add(new PriceRange(0, 499));
				priceRanges.add(new PriceRange(600, 2499));
				priceRanges.add(new PriceRange(2500, 70000));
				break;
			case 77:
				priceRanges.add(new PriceRange(0, 9));
				priceRanges.add(new PriceRange(10, 459));
				priceRanges.add(new PriceRange(460, 2499));
				priceRanges.add(new PriceRange(2500, 49999));
				break;
			case 76:
				priceRanges.add(new PriceRange(0, 9));
				priceRanges.add(new PriceRange(10, 599));
				priceRanges.add(new PriceRange(600, 2499));
				priceRanges.add(new PriceRange(2500, 199999));
				break;
			case 8215:
				priceRanges.add(new PriceRange(0, 9));
				priceRanges.add(new PriceRange(10, 419));
				priceRanges.add(new PriceRange(420, 2499));
				priceRanges.add(new PriceRange(2500, 59999));
				break;
		}
		return priceRanges;
	}
}
