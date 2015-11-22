package com.bhern.mongoqueries;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.group;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.limit;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.match;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.project;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.sort;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericXmlApplicationContext;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.util.StopWatch;

import com.bhern.mongoqueries.model.AggregationResult;
import com.bhern.mongoqueries.model.ExtendedAd;
import com.bhern.mongoqueries.model.MainCategory;
import com.bhern.mongoqueries.model.PriceRange;
import com.bhern.mongoqueries.model.Product;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

public class ExtendedDataExporter {
public static void main( String[] args ) throws ParseException{
    	ApplicationContext ctx = new GenericXmlApplicationContext("application-context.xml");
    	   MongoOperations mongoOperation = (MongoOperations)ctx.getBean("mongoTemplate");
    	
    	StopWatch stopwatch = new StopWatch();
    	stopwatch.start();
    	List<MainCategory> categories = DataInitializer.init();   	   
	   	System.out.print("Loaded!!");
	   	for(MainCategory cat : categories){	   		
	        HSSFWorkbook workbook = new HSSFWorkbook();
	   		for(PriceRange price : cat.getPriceRange()){
				   	Criteria criteria = getCriteria(cat.getValueId(), price.getMinPrice(), price.getMaxPrice());
				   	Aggregation sourceAggregation = getAggregation(criteria);	   	  
				   	
				   	AggregationResults<DBObject> aggregateResults = mongoOperation.aggregate(sourceAggregation, "extendedAd", DBObject.class);
				   	
				   	List<ExtendedAd> aggRes = mapResults(aggregateResults.getMappedResults());
				   	writeExcel(aggRes, cat.getName(), price, workbook );
	   		}
	   	}
	   	stopwatch.stop();
	   	System.out.println("Duration: " + stopwatch.getLastTaskTimeMillis());
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
	   				.push("productList").as("productList"),
	   			sort(Direction.DESC, "lastInspection"),
	   			project("adId", "imagesCount", "price", "textLenght", "lastInspection", "source", 
	   					"adCreated", "adPublish", "mainCategory", "postalCode", "productList")
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
            	aggRes.add(new ExtendedAd(adId, source, imagesCount, textLength, price, adCreated, adPublish,
            			lastInspection, products, null, postalCode));
            }
            
        }
        return aggRes;
    }
    
    private static void writeExcel(List<ExtendedAd> aggRes, String sheetName, PriceRange price, HSSFWorkbook workbook){
        //Write the results in a excel list
        HSSFSheet sheet = workbook.createSheet(price.getMinPrice() + " - " + price.getMaxPrice());
        CreationHelper createHelper = workbook.getCreationHelper();
        

        int rownum = 0;
        CellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setDataFormat(
                createHelper.createDataFormat().getFormat("yyyy-MM-dd'T'HH:mm:ss.SSSz")); 
        Row row = sheet.createRow(rownum++);
        Cell cell = row.createCell(0);
        cell.setCellValue("AdId");
        cell = row.createCell(1);
        cell.setCellValue("Source");
        cell = row.createCell(2);
        cell.setCellValue("Number of images");
        cell = row.createCell(3);
        cell.setCellValue("Text length");
        cell = row.createCell(4);
        cell.setCellValue("Price");
        cell = row.createCell(5);
        cell.setCellValue("Ad created date");
        cell = row.createCell(6);
        cell.setCellValue("Ad publish date");
        cell = row.createCell(7);
        cell.setCellValue("Last inspection date");
        cell = row.createCell(8);
        cell.setCellValue("Product list");
        cell = row.createCell(9);
        cell.setCellValue("Postal code");
        for(ExtendedAd res : aggRes){
            row = sheet.createRow(rownum++);
            int cellnum = 0;
            cell = row.createCell(cellnum++);
            cell.setCellValue(res.getAdId());
            cell = row.createCell(cellnum++);
            cell.setCellValue(res.getSource());
            cell = row.createCell(cellnum++);
            cell.setCellValue(res.getImagesCount());
            cell = row.createCell(cellnum++);
            cell.setCellValue(res.getTextLenght());           
            cell = row.createCell(cellnum++);
            cell.setCellValue(res.getPrice());
            cell = row.createCell(cellnum++);
            cell.setCellValue(res.getAdCreated());
            cell.setCellStyle(cellStyle);
            cell = row.createCell(cellnum++);
            cell.setCellValue(res.getAdPublish());
            cell.setCellStyle(cellStyle);
            cell = row.createCell(cellnum++);
            cell.setCellValue(res.getLastInspection());
            cell.setCellStyle(cellStyle);
            cell = row.createCell(cellnum++);
            cell = row.createCell(cellnum++);
            cell.setCellValue(res.getPostalCode());
        	
        }
         
        try {
            FileOutputStream out = 
                    new FileOutputStream(new File("C:\\Users\\blancaHN\\Desktop\\WI\\Master arbeit\\excels\\" + sheetName + ".xls"));
            workbook.write(out);
            out.close();
            System.out.println("Excel written successfully..");
             
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } 
    }
}
