package com.bhern.mongoqueries;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.group;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.match;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.project;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.sort;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericXmlApplicationContext;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.util.StopWatch;

import com.bhern.mongoqueries.model.ExtendedAd;
import com.bhern.mongoqueries.model.MainCategory;
import com.bhern.mongoqueries.model.PriceRange;
import com.bhern.mongoqueries.model.Product;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

import utils.ListUtils;


public class ExtendedAdPostgreSqlExporter {
	public static void main( String[] args ) throws ParseException, ClassNotFoundException, SQLException{
    	ApplicationContext ctx = new GenericXmlApplicationContext("application-context.xml");
    	MongoOperations mongoOperation = (MongoOperations)ctx.getBean("mongoTemplate");
    	
    	//Postgres connection
    	Connection connection = getPostgresConnection();
    	if(connection != null){    	
	    	StopWatch stopwatch = new StopWatch();
	    	stopwatch.start();
	    	List<MainCategory> categories = DataInitializer.init();  
		   	for(MainCategory cat : categories){	   	
		   		//String tableName = cat.getName().toLowerCase().replaceAll(" ", "_");
		   		String tableName = "extended_ad";
		   		createTable(tableName, connection.createStatement());
		   		for(PriceRange price : cat.getPriceRange()){
					   	Criteria criteria = getCriteria(cat.getValueId(), price.getMinPrice(), price.getMaxPrice());
					   	Aggregation sourceAggregation = getAggregation(criteria);
					   	AggregationResults<DBObject> aggregateResults = mongoOperation.aggregate(sourceAggregation, "extendedAd", DBObject.class);
					   	List<ExtendedAd> aggRes = mapResults(aggregateResults.getMappedResults());
					   	insertRows(aggRes, connection, tableName);
		   		}
		   	}
		   	stopwatch.stop();
		   	System.out.println("Duration: " + stopwatch.getLastTaskTimeMillis());
    	}
    }

	private static Connection getPostgresConnection() {
		Connection connection = null;
		try {
			Class.forName("org.postgresql.Driver");
		} catch (ClassNotFoundException e) {
			System.out.println("Where is your PostgreSQL JDBC Driver? "
					+ "Include in your library path!");
			e.printStackTrace();
			return connection;
		}

		System.out.println("PostgreSQL JDBC Driver Registered!");
		try {
			connection = DriverManager.getConnection(
					"jdbc:postgresql://127.0.0.1:5432/analysis", "blanca",
					"123456");
		} catch (SQLException e) {
			System.out.println("Connection Failed! Check output console");
			e.printStackTrace();
			return connection;
		}
		if (connection != null) {
			System.out.println("You made it, take control your database now!");
		} else {
			System.out.println("Failed to make connection!");
		}
		return connection;
	}
    
    private static Criteria getCriteria(int valueId, int minPrice, int maxPrice){
    	Criteria criteria = new Criteria().andOperator
	   			(Criteria.where("mainCategory.valueId").is(valueId), 
	   		     Criteria.where("price").lt(maxPrice), 
	   		     Criteria.where("price").gt(minPrice)
	   					);
    	return criteria;
    }
    
    private static Aggregation getAggregation(Criteria matchCriteria){
    	return newAggregation( 
	   			match(matchCriteria), 
	   			group("adId")
	   				.count().as("count")
	   				.first("source").as("source")
	   				.first("imagesCount").as("imagesCount")
	   				.first("price").as("price")
	   				.first("textLenght").as("textLenght")
	   				.first("lastInspection").as("lastInspection")
	   				.first("adCreated").as("adCreated")
	   				.first("adPublish").as("adPublish")
	   				.first("mainCategory.valueId").as("mainCategory")
	   				.first("postalCode").as("postalCode")
	   				.first("orgId").as("orgId")
	   				.push("productList").as("productList"),
	   			sort(Direction.DESC, "lastInspection"),
	   			project("adId", "imagesCount", "price", "textLenght", "lastInspection", "source", 
	   					"adCreated", "adPublish", "mainCategory", "postalCode", "productList", "orgId")
		);
    }
    
    private static List<ExtendedAd> mapResults(List<DBObject> fieldList){
    	List<ExtendedAd> aggRes = new ArrayList();
        if(fieldList != null && !fieldList.isEmpty()) {
        	Long adId = null;
        	List<Date> inspectionDates = new ArrayList();
        	HashMap<Long, List<String>> map = new HashMap();
            for(DBObject db: fieldList){
            	String source = db.get("source").toString();
            	Integer imagesCount = Integer.valueOf(db.get("imagesCount").toString());
            	Date lastInspection = ((Date)db.get("lastInspection"));
            	Date adCreated = ((Date)db.get("adCreated"));
            	Date adPublish = ((Date)db.get("adPublish"));
            	Integer textLength = db.get("textLenght") == null ? 0:Integer.valueOf(db.get("textLenght").toString());
            	Integer mainCategory = Integer.valueOf(db.get("mainCategory").toString());
            	String postalCode = db.get("postalCode").toString();
            	Double price = Double.valueOf(db.get("price").toString());
            	Long orgId = Long.valueOf(db.get("orgId").toString());
            	adId = Long.valueOf(db.get("_id").toString());
            	List<Product> products = new ArrayList();
            	for(Object productList : (BasicDBList)db.get("productList")){
            		for(Object product : (BasicDBList)productList){
            			Long featureId = ((BasicDBObject)product).get("featureId") == null ? 0:Long.valueOf(((BasicDBObject)product).get("featureId").toString());
            			String productType = ((BasicDBObject)product).get("productType") == null ? "":((BasicDBObject)product).get("productType").toString();
            			String productName = ((BasicDBObject)product).get("productName") == null ? "":((BasicDBObject)product).get("productName").toString();
                    	Double productPrice = ((BasicDBObject)product).get("price") == null ? 0:Double.valueOf(((BasicDBObject)product).get("price").toString());
            			products.add(new Product(featureId, productType, productName, productPrice));
            		}
            	}
            	aggRes.add(new ExtendedAd(orgId, adId, source, imagesCount, textLength, price, adCreated, adPublish,
            			lastInspection, products, null, postalCode));
            }
            
        }
        return aggRes;
    }
    
    private static void insertRows(List<ExtendedAd> aggRes, Connection c, String tableName){
    	String sql = "";
        try {
	        for(ExtendedAd res : aggRes){
	        	Statement stmt = c.createStatement();
	            sql = "INSERT INTO "+ tableName + " (ad_id,source,number_images,"
	            		+ "text_length,price,"
	            		//+ ",ad_created,ad_publish,ad_last_inspection,"
	            		+ "number_products,products_total_price,postal_code,org_id,source_name) "
	                  + "VALUES ("+res.getAdId()+","
	                  		+ res.getSource()+","
	                  		+ res.getImagesCount()+","
	                  		+ res.getTextLenght() + ","
	                  		+ res.getPrice() + ","
	                  		/**+ res.getAdCreated() + ","
	                  		+ res.getAdPublish() + ","
	                  		+ res.getLastInspection() + ","**/
	                  		+ ListUtils.getListSize(res.getProductList()) + ","
	                  		+ ListUtils.getProductsPrice(res.getProductList()) + ","
	                  		+ "'" + res.getPostalCode() + "' ,"
	                  		+ res.getOrgId() + ","
	                  		+ "'" + ListUtils.getSourceName(res.getSource()) + "'"
	                  		+ ")";
					stmt.executeUpdate(sql);
	        }
		} catch (SQLException e) {
			System.out.println("Problem inserting " + sql);
			e.printStackTrace();
		}
        System.out.println("Inserted elements: " + aggRes.size());
    }
    
    private static void createTable(String tableName, Statement stmt){
    	String query = "create table if not exists "+ tableName + "(" + 
						"ad_id bigint not null," +  
						"source int not null, " + 
						"number_images int, " + 
						"text_length bigint, " + 
						"price decimal, " + 
						/**"ad_created timestamp, " + 
						"ad_publish timestamp , " + 
						"ad_last_inspection timestamp ," +  **/
						"number_products int, " + 
						"products_total_price decimal," +  
						"postal_code varchar, " + 
						"org_id bigint, " + 
						"source_name varchar, " + 
						"PRIMARY KEY( ad_id )" + 
					")";
    	try {
			stmt.executeUpdate(query);
		} catch (SQLException e) {
			System.out.println("Problem creating table " + tableName);
			e.printStackTrace();
		}

		System.out.println("Creating table " + tableName);
    }
}
