package com.bhern.mongoqueries;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.group;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.limit;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.match;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregationOptions;
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
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericXmlApplicationContext;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;

import com.mongodb.DBObject;

public class AdIdAggregationApp {
	public static void main( String[] args ) throws ParseException{
		ApplicationContext ctx = new GenericXmlApplicationContext("application-context.xml");
		   MongoOperations mongoOperation = (MongoOperations)ctx.getBean("mongoTemplate");
		   
		   Aggregation agg = newAggregation( 
		   			group("adId"),
		   			project("adId")
			).withOptions(newAggregationOptions().allowDiskUse(true).build());
		   
		   List<DBObject> res = mongoOperation.aggregate(agg, "fcsAdMarketPlace", DBObject.class).getMappedResults();

		   HSSFWorkbook workbook = new HSSFWorkbook();
		   if(res != null && !res.isEmpty()) {
			 HSSFSheet sheet = workbook.createSheet("adIDs");
			 Long adId = null;
			 int rownum = 0;
		     for(DBObject db: res){
		    	 Row row = sheet.createRow(rownum++);
		    	 Cell cell = row.createCell(0);
		     	adId = Long.valueOf(db.get("_id").toString());
		     	cell.setCellValue(adId);
		     }
		 }
		 
		 try {
	            FileOutputStream out = 
	                    new FileOutputStream(new File("C:\\Users\\blancaHN\\Desktop\\WI\\Master arbeit\\excels\\adIds.xls"));
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
