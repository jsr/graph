package com.mongodb.graph.test.utils;

import java.util.Date;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

@SuppressWarnings("serial")
public class WorksForEdge extends BasicDBObject {

	private static final String START_KEY = "start_date";
	private static final String END_KEY = "end_date";

	public WorksForEdge(DBObject data){
		this.putAll(data);
	}
	
	public WorksForEdge(Date start, Date end){
		this.put(START_KEY, start);
		if(end != null)
			this.put(END_KEY, end);
	}
	
	public Date getStartDate(){
		return (Date) this.get(START_KEY);
	}
	
	public Date getEndDate(){
		return (Date) this.get(END_KEY);
	}		
}
