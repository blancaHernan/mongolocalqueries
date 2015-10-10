package com.bhern.mongoqueries;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.group;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.limit;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.match;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.project;

import java.util.List;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericXmlApplicationContext;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.query.Criteria;

import com.bhern.mongoqueries.model.AggregationResult;

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
	   	
	   	List<AggregationResult> res = mongoOperation.aggregate(agg, "fcsAdMarketPlace", AggregationResult.class)
	   			.getMappedResults();
	   	
	   	if(res != null){
	   		System.out.println("Finish! " + res.size());
	   	}
    }
}
