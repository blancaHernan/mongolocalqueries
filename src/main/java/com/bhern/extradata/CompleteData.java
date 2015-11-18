package com.bhern.extradata;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.match;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;

import java.io.FileInputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;

import org.apache.poi.ss.usermodel.DataFormat;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericXmlApplicationContext;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.format.datetime.DateFormatter;
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
 	   	//Read all the documents
		for(int i = 5; i<=9; i++){
			FileInputStream inputStream = null;
			Scanner sc = null;
			String path = "C:\\Users\\blancaHN\\Desktop\\WI\\Master arbeit\\Nihal Data\\2015 0" + i + ".skv";
	
			try {
				StopWatch stopwatch = new StopWatch();
		    	stopwatch.start();
			    inputStream = new FileInputStream(path);
			    sc = new Scanner(inputStream, "UTF-8");
			    // note that Scanner suppresses exceptions
			    if (sc.ioException() != null) {
			        throw sc.ioException();
			    }
			    Map<String, ExtendedAd> map = fillInMap(sc);
			    stopwatch.stop();

				//Store all the ExtendedAd in the DB
				//mongoOperation.insertAll(map.values());
			    System.out.println(path + " --> Elements: " + map.size() + " Time " + stopwatch.getTotalTimeMillis());
			} finally {
			    if (inputStream != null) {
			        inputStream.close();
			    }
			    if (sc != null) {
			        sc.close();
			    }
			}
		}
	}

	private static Map<String, ExtendedAd> fillInMap(Scanner sc){
		Map<String, ExtendedAd> map = new HashMap(); 
	    while (sc.hasNextLine()) {
	        String line = sc.nextLine();
	    	try{//If there is an exception for one line, the document must be read anyways	        
		        String[] fields = line.split(";");
		        //I know that this does not produce a out of bound exception
		        String adId = fields[1];
		        if(adId != null && adId.matches("[-+]?\\d*\\.?\\d+")){		        
			        AggregationResults<DBObject> sourceResults = mongoOperation.aggregate(
			        		newAggregation(match(Criteria.where("adId").is(Long.valueOf(adId)))), 
			        		"fcsAdAggregate", DBObject.class);
			        if(sourceResults != null && !sourceResults.getMappedResults().isEmpty()){
			        	Product product = getProduct(fields);
			        	for(DBObject ad : sourceResults.getMappedResults()){
			        		ExtendedAd extendedAd = map.get(adId);
			        		if(extendedAd!= null){
			        			extendedAd.getProductList().add(product);
			        			//System.out.println("The adId " + adId + " has been already added " + extendedAd.getProductList().size());
			        		}else{
			        			DateFormatter dateFormat = new DateFormatter("dd.MM.yyyy HH:mm:ss");
			        			Date adCreatedDate = dateFormat.parse(fields[3], Locale.GERMAN);
								Date adPublishedDate = dateFormat.parse(fields[4], Locale.GERMAN);
			        			extendedAd = new ExtendedAd(Long.valueOf(adId), 
			        					(String)ad.get("source"), 
			        					((org.bson.types.BasicBSONList)ad.get("images")).size(),
			        					((String)ad.get("text")).length(),
			        					(Double)ad.get("price"),
			        					adCreatedDate,
			        					adPublishedDate,
			        					new Date(),
			        					new ArrayList(), 
			        					new MainCategory("", (Integer) ((com.mongodb.BasicDBObject)ad.get("mainCategory")).get("valueId"), null), 
			        					((String)ad.get("postalCode"))
			        					);
			        			extendedAd.getProductList().add(product);
			        			map.put(adId, extendedAd);
			        			//System.out.println("The adId " + adId + " has been new added.");
			        			if(map.size() > 500000){
			        				mongoOperation.insertAll(map.values());
			        			    System.out.println("Middle --> Elements: " + map.size());
			        				map.clear();
			        			}
			        		}
			        	}
			        }
		        }
	    	} catch(Exception e){
	    		System.out.println("Exception for the line " + line + " " + e);
	    	}
	    } 
	    return map;
	}
	
	private static Product getProduct(String[] fields){
		//Fill product
    	Product product = new Product();
    	try{
    		product = new Product(Long.valueOf(fields[6]), fields[7], 
    			fields[8], Double.valueOf(fields[9].replace(",", ".")));
    	}catch(Exception e){
    		//Do nothing, but the ad should be added to the DB wothout product
    	}
    	return product;
	}
}
