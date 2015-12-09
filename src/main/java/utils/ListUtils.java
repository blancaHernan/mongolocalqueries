package utils;

import java.util.List;

import com.bhern.mongoqueries.model.Product;

public class ListUtils {
	public static int getListSize(List list){
		return list == null ? 0:list.size();
	}
	
	public static double getProductsPrice(List<Product> list){
		double price = 0;
		if(list != null){
			for(Product prod : list){
				price += prod.getPrice();
			}
		}
		return price;
	}
	
	public static String getSourceName(String source){
    	String sourceName = "Irrelevant";
    	if(source.equalsIgnoreCase("1")){
    		sourceName = "Desktop";
    	}
    	if(source.equalsIgnoreCase("9") || source.equalsIgnoreCase("10") 
    			|| source.equalsIgnoreCase("11") || source.equalsIgnoreCase("12")){
    		sourceName = "Mobile";
    	}
    	
    	return sourceName;
    }
}
