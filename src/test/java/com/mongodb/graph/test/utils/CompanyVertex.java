package com.mongodb.graph.test.utils;

import com.mongodb.DBObject;

public class CompanyVertex extends VertexDataObject {
	
	private static final String COMPANY_LABEL = "Company";
	private static final String NAME_KEY = "name";

	public CompanyVertex(DBObject data){
		super(data);
	}
	
	public CompanyVertex(String name){
		super();
		this._dbObject.put(NAME_KEY, name);
	}
	
	public String getName(){
		return (String) this._dbObject.get(NAME_KEY);
	}
	
	@Override
	protected String getType() {
		return COMPANY_LABEL;
	}
}
