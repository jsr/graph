package com.mongodb.graph.test.utils;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

@SuppressWarnings("serial")
public class FamilyEdge extends BasicDBObject {

	private static final String RELATION_KEY = "relation";

	public FamilyEdge(DBObject data){
		this.putAll(data);
	}
	
	public FamilyEdge(String relation){
		this.put(RELATION_KEY, relation);
	}
	
	public String getRelation(){
		return (String) this.get(RELATION_KEY);
	}	
}
