package com.mongodb.graph.test.utils;

import com.mongodb.DBObject;

public class PersonVertex extends VertexDataObject {
	
	private static final String PERSON_LABEL = "Person";
	private static final String NAME_KEY = "name";

	public PersonVertex(DBObject data){
		super(data);
	}
	
	public PersonVertex(String name){
		super();
		this._dbObject.put(NAME_KEY, name);
	}
	
	public String getName(){
		return (String) this._dbObject.get(NAME_KEY);
	}
	
	@Override
	protected String getType() {
		return PERSON_LABEL;
	}
}
