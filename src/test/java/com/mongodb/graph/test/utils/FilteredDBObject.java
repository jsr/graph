package com.mongodb.graph.test.utils;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

@SuppressWarnings("serial")
public class FilteredDBObject extends BasicDBObject {
	
	private static final String ID_KEY = "_id";

	public static BasicDBObject withoutId(DBObject source){
		return new FilteredDBObject(source, ID_KEY);
	}
	
	public static BasicDBObject filterObject(DBObject source, String... keysToHide){
		return new FilteredDBObject(source, keysToHide);
	}
	
	public FilteredDBObject(DBObject source, String... keysToHide){
		this.putAll(source);
		for(String key : keysToHide){
			this.remove(key);
		}
	}
}
