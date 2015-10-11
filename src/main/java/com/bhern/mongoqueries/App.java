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
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

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

import com.bhern.mongoqueries.model.AggregationResult;
import  com.mongodb.BasicDBList;
import com.mongodb.DBObject;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args ) throws ParseException{
    	ApplicationContext ctx = new GenericXmlApplicationContext("application-context.xml");
    	   MongoOperations mongoOperation = (MongoOperations)ctx.getBean("mongoTemplate");
    	
    	
	   	System.out.print("Loaded!!");
	   	Criteria criteria = new Criteria().andOperator
	   			(Criteria.where("mainCategory.valueId").is(76), 
	   		     Criteria.where("price").lt(9)
	   					);
	   	Aggregation sourceAggregation = newAggregation( 
	   			match(criteria), 
	   			limit(100),
	   			group("adId").push("source").as("source"),
	   			project("source")
		);
	   	
	   	Aggregation inspectionDateAggregation = newAggregation( 
	   			match(criteria), 
	   			limit(100),
	   			group("adId").push("inspectionDate").as("inspectionDate"),
	   			sort(Direction.DESC, "inspectionDate"),
	   			project("inspectionDate")
		);
	   	
	   	AggregationResults<DBObject> sourceResults = mongoOperation.aggregate(sourceAggregation, "fcsAdMarketPlace", DBObject.class);
	   	AggregationResults<DBObject> inspectionDateResults = mongoOperation.aggregate(inspectionDateAggregation, "fcsAdMarketPlace", DBObject.class);
	   	
	   	List<DBObject> sourceFieldList = sourceResults.getMappedResults();
	   	List<DBObject> inspectionDateFieldList = inspectionDateResults.getMappedResults();
	   	List<AggregationResult> aggRes = new ArrayList();
        if(sourceFieldList != null && !sourceFieldList.isEmpty()) {
        	Long adId = null;
        	List<String> sources = new ArrayList();
        	List<Date> inspectionDates = new ArrayList();
        	HashMap<Long, List<String>> map = new HashMap();
            for(DBObject db: sourceFieldList){
            	adId = Long.valueOf(db.get("_id").toString());
            	for(Object source : ((BasicDBList)db.get("source"))){
            		sources.add((String) source);
            	}
            	map.put(adId, sources);
            }
            for(DBObject db: inspectionDateFieldList){
            	adId = Long.valueOf(db.get("_id").toString());
            	for(Object inspectionDate : ((BasicDBList)db.get("inspectionDate"))){
            		inspectionDates.add((Date)inspectionDate);
            	}
            	
            	aggRes.add(new AggregationResult(adId, map.get(adId), inspectionDates));
            }
        }
        
        //Write the results in a excel list
        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet("Sample sheet");
        CreationHelper createHelper = workbook.getCreationHelper();
        

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
        cell = row.createCell(4);
        cell.setCellValue("First inspectionDate");
        cell = row.createCell(5);
        cell.setCellValue("Last inspectionDate");
        for(AggregationResult res : aggRes){
            row = sheet.createRow(rownum++);
            int cellnum = 0;
            cell = row.createCell(cellnum++);
            cell.setCellValue(res.getAdId());
            cell = row.createCell(cellnum++);
            cell.setCellValue(res.getSource().size());
            //TODO: first and last source
            cell = row.createCell(cellnum++);
            cell = row.createCell(cellnum++);

            CellStyle cellStyle = workbook.createCellStyle();
            cellStyle.setDataFormat(
                createHelper.createDataFormat().getFormat("yyyy-MM-dd'T'HH:mm:ss.SSSz")); //2014-06-30T00:01:43.289Z           
            cell = row.createCell(cellnum++);
            cell.setCellValue(res.getInspectionDate().get(0));
            cell.setCellStyle(cellStyle);
            cell = row.createCell(cellnum++);
            cell.setCellValue(res.getInspectionDate().get(res.getInspectionDate().size() - 1));
            cell.setCellStyle(cellStyle);
        	
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
