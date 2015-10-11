package com.bhern.mongoqueries;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.group;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.limit;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.match;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.project;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericXmlApplicationContext;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;

import com.bhern.mongoqueries.model.AggregationResult;
import  com.mongodb.BasicDBList;
import com.mongodb.DBObject;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args ){
    	ApplicationContext ctx = new GenericXmlApplicationContext("application-context.xml");
    	   MongoOperations mongoOperation = (MongoOperations)ctx.getBean("mongoTemplate");
    	
    	
	   	System.out.print("Loaded!!");
	   	Criteria criteria = new Criteria().andOperator
	   			(Criteria.where("mainCategory.valueId").is(76), 
	   		     Criteria.where("price").lt(9)
	   					);
	   	Aggregation agg = newAggregation( 
	   			match(criteria), 
	   			limit(100),
	   			group("adId").push("source").as("source"),
	   			project("source")
		);
	   	
	   	AggregationResults<DBObject> results = mongoOperation.aggregate(agg, "fcsAdMarketPlace", DBObject.class);
	   	
	   	List<DBObject> fieldList = results.getMappedResults();
	   	List<AggregationResult> aggRes = new ArrayList();
        if(fieldList != null && !fieldList.isEmpty()) {
            for(DBObject db: fieldList){
            	db.get("_id").toString();
            	db.get("source").toString();
            	List<String> sources = new ArrayList();
            	for(Object source : ((BasicDBList)db.get("source"))){
            		sources.add((String) source);
            	}
            	
            	aggRes.add(new AggregationResult(Long.valueOf(db.get("_id").toString()), sources));
            }
        }
        
        //Write the results in a excel list
        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet("Sample sheet");
        

        int rownum = 0;
        Row row = sheet.createRow(rownum++);
        Cell cell = row.createCell(0);
        cell.setCellValue("AdId");
        cell = row.createCell(1);
        cell.setCellValue("Number of sources");
        cell = row.createCell(2);
        cell.setCellValue("First source");
        cell = row.createCell(3);
        cell.setCellValue("Last source");
        for(AggregationResult res : aggRes){
            row = sheet.createRow(rownum++);
            int cellnum = 0;
            cell = row.createCell(cellnum++);
            cell.setCellValue(res.getAdId());
            cell = row.createCell(cellnum++);
            cell.setCellValue(res.getSource().size());
        	
        }
         
        try {
            FileOutputStream out = 
                    new FileOutputStream(new File("C:\\Users\\blancaHN\\Desktop\\WI\\Master arbeit\\excels\\test.xls"));
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
