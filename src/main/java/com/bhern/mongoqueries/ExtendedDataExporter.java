package com.bhern.mongoqueries;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.group;
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
import com.bhern.mongoqueries.model.MainCateogry;
import com.bhern.mongoqueries.model.PriceRange;
import com.mongodb.BasicDBList;
import com.mongodb.DBObject;

public class ExtendedDataExporter {
	public static void main( String[] args ) throws ParseException{
    	ApplicationContext ctx = new GenericXmlApplicationContext("application-context.xml");
    	MongoOperations mongoOperation = (MongoOperations)ctx.getBean("mongoTemplate");
    	
    	StopWatch stopwatch = new StopWatch();
    	stopwatch.start();
    	List<MainCateogry> categories = DataInitializer.init();   	   
	   	System.out.print("Loaded!!");
	   	for(MainCateogry cat : categories){
	        HSSFWorkbook workbook = new HSSFWorkbook();
	   		for(PriceRange price : cat.getPriceRange()){
	   			try{
				   	Criteria criteria = getCriteria(cat.getValueId(), price.getMinPrice(), price.getMaxPrice());
				   	Aggregation sourceAggregation = getAggregation(criteria, "source");	   	
				   	Aggregation inspectionDateAggregation = getAggregation(criteria, "inspectionDate");	   
				   	
				   	AggregationResults<DBObject> sourceResults = mongoOperation.aggregate(sourceAggregation, "fcsAdMarketPlace", DBObject.class);
				   	AggregationResults<DBObject> inspectionDateResults = mongoOperation.aggregate(inspectionDateAggregation, "fcsAdMarketPlace", DBObject.class);
				   	
				   	List<AggregationResult> aggRes = mapResults(sourceResults.getMappedResults(), inspectionDateResults.getMappedResults());
				   	writeExcel(aggRes, cat.getName(), price, workbook );
	   			} catch(Exception e){
	   				System.out.print("There where some errors with the cat " + cat.getName() +
	   						" and price " + price.getMinPrice());
	   				System.out.println(e);
	   			}
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
    
    private static Aggregation getAggregation(Criteria matchCriteria, String pushFiled){
    	return newAggregation( 
	   			match(matchCriteria), 
	   			//limit(100),
	   			group("adId", "inspectionDate").push(pushFiled).as(pushFiled),
	   			sort(Direction.DESC, "inspectionDate"),
	   			project(pushFiled)
		);
    }
    
    private static List<AggregationResult> mapResults(List<DBObject> sourceFieldList, List<DBObject> inspectionDateFieldList){
    	List<AggregationResult> aggRes = new ArrayList();
        if(sourceFieldList != null && !sourceFieldList.isEmpty()) {
        	Long adId = null;
        	List<Date> inspectionDates = new ArrayList();
        	HashMap<Long, List<String>> map = new HashMap();
            for(DBObject db: sourceFieldList){
            	adId = Long.valueOf(db.get("adId").toString());
            	List<String> sources = new ArrayList();
            	for(Object source : ((BasicDBList)db.get("source"))){
            		sources.add((String) source);
            	}
            	map.put(adId, sources);
            }
            for(DBObject db: inspectionDateFieldList){
            	adId = Long.valueOf(db.get("adId").toString());
            	for(Object inspectionDate : ((BasicDBList)db.get("inspectionDate"))){
            		inspectionDates.add((Date)inspectionDate);
            	}
            	
            	aggRes.add(new AggregationResult(adId, map.get(adId), inspectionDates));
            }
        }
        return aggRes;
    }
    
    private static void writeExcel(List<AggregationResult> aggRes, String sheetName, PriceRange price, HSSFWorkbook workbook){
        //Write the results in a excel list
        HSSFSheet sheet = workbook.createSheet(price.getMinPrice() + " - " + price.getMaxPrice());
        CreationHelper createHelper = workbook.getCreationHelper();
        

        int rownum = 0;
        Row row = sheet.createRow(rownum++);
        Cell cell = row.createCell(0);
        cell.setCellValue("AdId");
        cell = row.createCell(1);
        cell.setCellValue("Number of versions");
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
            cell.setCellValue(res.getInspectionDate().size());
            cell = row.createCell(cellnum++);
            cell.setCellValue(res.getSource().get(0));
            cell = row.createCell(cellnum++);
            cell.setCellValue(res.getSource().get(res.getSource().size() - 1));

            CellStyle cellStyle = workbook.createCellStyle();
            cellStyle.setDataFormat(
                createHelper.createDataFormat().getFormat("yyyy-MM-dd'T'HH:mm:ss.SSSz")); //2014-06-30T00:01:43.289Z           
            cell = row.createCell(cellnum++);
            cell.setCellValue(res.getInspectionDate().	get(0));
            cell.setCellStyle(cellStyle);
            cell = row.createCell(cellnum++);
            cell.setCellValue(res.getInspectionDate().get(res.getInspectionDate().size() - 1));
            cell.setCellStyle(cellStyle);
        	
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
