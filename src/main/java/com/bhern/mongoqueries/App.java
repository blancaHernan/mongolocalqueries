package com.bhern.mongoqueries;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.GenericXmlApplicationContext;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.group;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.match;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.project;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.sort;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.limit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;
import java.util.List;

import com.bhern.mongoqueries.model.AggregationResult;
import com.bhern.mongoqueries.mongoclient.SpringMongoConfig;

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
