package com.bhern.mongoqueries.users;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.group;
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
import org.springframework.util.StopWatch;

import com.mongodb.BasicDBList;
import com.mongodb.DBObject;

public class UserAnalyzer {
	private static List<Long> orgIdsToSkip = new ArrayList();
	
	public static void main(String[] args){
		ApplicationContext ctx = new GenericXmlApplicationContext("application-context.xml");
    	MongoOperations mongoOperation = (MongoOperations)ctx.getBean("mongoTemplate");
    	
    	List<User> userList = new ArrayList();
    	StopWatch stopwatch = new StopWatch();
    	stopwatch.start();
    	boolean search = true;
    	//Last input 
    	//	52923 elements in User-19970006-19975006
    	long minOrgId = 19560006;
    	long maxOrgId = 19565006;        
    	//Write the results in a excel list
    	HSSFWorkbook workBook = new HSSFWorkbook();
    	while(search){
        	Aggregation agg = getAggregation(minOrgId, maxOrgId);
			AggregationResults<DBObject> aggregateResults = mongoOperation.aggregate(agg, "fcsAdMarketPlace", DBObject.class);
			List<DBObject> mappedResults = aggregateResults.getMappedResults();
			if(!(mappedResults == null || mappedResults.isEmpty())){
				userList.addAll(getMappedResults(mappedResults));				
				writeExcel(userList, minOrgId, maxOrgId, workBook);
		    	minOrgId = maxOrgId;
		    	maxOrgId += 5000;   
			} else{
				search = false;
			}
    	}  	
    	stopwatch.stop();
	}

	private static List<User> getMappedResults(List<DBObject> fieldList){
		List<User> userList = new ArrayList();
		for(DBObject db: fieldList){
			List<Long> adIds = new ArrayList();
			List<Integer> mainCategoryIds = new ArrayList();
			List<String> adStatus = new ArrayList(), sources = new ArrayList();
			Long orgId = (Long)db.get("_id");
			for(Object productList : (BasicDBList)db.get("adIds")){
				adIds.add((Long)productList);
			}
			for(Object productList : (BasicDBList)db.get("mainCategoryValueId")){
				mainCategoryIds.add((Integer)productList);
			}
			for(Object productList : (BasicDBList)db.get("sources")){
				sources.add((String)productList);
			}
			for(Object productList : (BasicDBList)db.get("adStatus")){
				adStatus.add((String)productList);
			}
			userList.add(new User(orgId, adIds, mainCategoryIds, adStatus, sources));
			orgIdsToSkip.add(orgId);
		}
		return userList;
	}
	
    private static Aggregation getAggregation(long minOrgId, long maxOrgId){
    	return newAggregation(
    			match(Criteria.where("orgId").gt(minOrgId)
    					.andOperator(Criteria.where("orgId").lt(maxOrgId))),
	   			group("orgId")
	   			.push("adId").as("adIds")
   				.push("adStatus").as("adStatus")
   				.push("source").as("sources")
   				.push("mainCategory.valueId").as("mainCategoryValueId"),
	   			project("orgId", "adIds", "adStatus", "sources", "mainCategoryValueId")
		);
    }
    
    private static void writeExcel(List<User> aggRes, long minOrgId, long maxOrgId, HSSFWorkbook workBook){
    	String sheetName = "User-" + minOrgId + "-" + maxOrgId;
        HSSFSheet sheet = workBook.createSheet(sheetName);
        int rownum = 0;
        Row row = sheet.createRow(rownum++);
        Cell cell = row.createCell(0);
        cell.setCellValue("OrgdId");
        cell = row.createCell(1);
        cell.setCellValue("Number of ads");
        cell = row.createCell(2);
        cell.setCellValue("Number of categories");
        cell = row.createCell(3);
        cell.setCellValue("Number of sources");
        cell = row.createCell(4);
        cell.setCellValue("Number of active ads");
        cell = row.createCell(5);
        cell.setCellValue("Number of pending ads");
        cell = row.createCell(6);
        cell.setCellValue("Categories");
        cell = row.createCell(7);
        cell.setCellValue("Sources");
        for(User res : aggRes){
            row = sheet.createRow(rownum++);
            int cellnum = 0;
            cell = row.createCell(cellnum++);
            cell.setCellValue(res.getOrgId());
            cell = row.createCell(cellnum++);
            cell.setCellValue(res.getNumberAds());
            cell = row.createCell(cellnum++);
            cell.setCellValue(res.getNumberOfCategories());
            cell = row.createCell(cellnum++);
            cell.setCellValue(res.getNumberOfSources());           
            cell = row.createCell(cellnum++);
            cell.setCellValue(res.getNumberOfActiveAds());
            cell = row.createCell(cellnum++);
            cell.setCellValue(res.getNumberOfPendingAds());
            cell = row.createCell(cellnum++);
            cell.setCellValue(res.getMainCategoryIds().toString());
            cell = row.createCell(cellnum++);
            cell.setCellValue(res.getSources().toString());
        }
         
        try {
            FileOutputStream out = 
                    new FileOutputStream(new File("C:\\Users\\blancaHN\\Desktop\\WI\\Master arbeit\\excels\\users\\users_1.xls"));
            workBook.write(out);
            out.close();
            System.out.println(aggRes.size() + " elements in " + sheetName);
             
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } 
    }
}
