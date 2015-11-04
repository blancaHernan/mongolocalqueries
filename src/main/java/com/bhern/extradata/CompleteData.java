package com.bhern.extradata;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.match;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Scanner;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericXmlApplicationContext;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.util.StopWatch;

import com.bhern.mongoqueries.model.ExtendedAd;
import com.bhern.mongoqueries.model.MainCategory;
import com.bhern.mongoqueries.model.Product;
import com.mongodb.DBObject;

/**
 * Extra data information 
 * 
 * @author blancaHN
 *
 */
public class CompleteData {
	private static ApplicationContext ctx = new GenericXmlApplicationContext("application-context.xml");
   	private static MongoOperations mongoOperation = (MongoOperations)ctx.getBean("mongoTemplate");

	public static void main(String[] args) throws IOException{
 	   	
 	   	//Read the file
		FileInputStream inputStream = null;
		Scanner sc = null;
		String path = "C:\\Users\\blancaHN\\OneDrive\\WI\\Master arbeit\\Nihal Data\\2015 07.skv";

		try {
			StopWatch stopwatch = new StopWatch();
	    	stopwatch.start();
		    inputStream = new FileInputStream(path);
		    sc = new Scanner(inputStream, "UTF-8");
		    // note that Scanner suppresses exceptions
		    if (sc.ioException() != null) {
		        throw sc.ioException();
		    }
		    HashMap<String, ExtendedAd>  map = getMap(sc);
		    stopwatch.stop();
		    
		    System.out.println("Elements: " + map.size() + " Time " + stopwatch.getTotalTimeMillis());
		} finally {
		    if (inputStream != null) {
		        inputStream.close();
		    }
		    if (sc != null) {
		        sc.close();
		    }
		}
	}

	private static HashMap<String, ExtendedAd> getMap(Scanner sc){
		HashMap<String, ExtendedAd> map = new HashMap(); 
	    while (sc.hasNextLine()) {
	        String line = sc.nextLine();
	        
	        String[] fields = line.split(";");
	        //I know that this does not produce a out of bound exception
	        String adId = fields[1];
	        if(adId != null && adId.matches("[-+]?\\d*\\.?\\d+")){		        
		        AggregationResults<DBObject> sourceResults = mongoOperation.aggregate(
		        		newAggregation(match(Criteria.where("adId").is(Long.valueOf(adId)))), 
		        		"fcsAdAggregate", DBObject.class);
		        if(sourceResults != null && !sourceResults.getMappedResults().isEmpty()){
		        	//Fill product
		        	Product product = new Product(Long.valueOf(fields[6]), fields[7], 
		        			fields[8], Double.valueOf(fields[9].replace(",", ".")));
		        	for(DBObject ad : sourceResults.getMappedResults()){
		        		ExtendedAd extendedAd = map.get(adId);
		        		if(extendedAd!= null){
		        			extendedAd.getProductList().add(product);
		        			System.out.println("The adId " + adId + " has been already added " + extendedAd.getProductList().size());
		        		}else{
		        			extendedAd = new ExtendedAd(Long.valueOf(adId), 
		        					(String)ad.get("source"), 
		        					((org.bson.types.BasicBSONList)ad.get("images")).size(),
		        					((String)ad.get("text")).length(),
		        					(Double)ad.get("price"),
		        					new Date(),
		        					new Date(),
		        					new Date(),
		        					new ArrayList(), 
		        					new MainCategory("", (Integer) ((com.mongodb.BasicDBObject)ad.get("mainCategory")).get("valueId"), null)
		        					);
		        			extendedAd.getProductList().add(product);
		        			map.put(adId, extendedAd);
		        			System.out.println("The adId " + adId + " added.");
		        		}
		        	}
		        }
	        }
	    } 
	    return map;
	}
}
