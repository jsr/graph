package com.mongodb.graph.test.utils;

import org.bson.types.ObjectId;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

public abstract class VertexDataObject {

	private static final String ID_KEY = "_id";
	private static final String TYPE_KEY = "_type";
	
	protected DBObject _dbObject = null;
	
	public VertexDataObject(DBObject dbObj){
		if(dbObj != null)
			_dbObject = dbObj;
		else
			_dbObject = new BasicDBObject();
	}
	
	public VertexDataObject(){
		_dbObject = new BasicDBObject();
		this._dbObject.put(ID_KEY, new ObjectId());
	}
	
	public DBObject toDBObject(){		
		return _dbObject;
	}	
	
	@Override
	public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (obj == this)
            return true;
        if (!(obj instanceof VertexDataObject))
            return false;

        VertexDataObject rhs = (VertexDataObject) obj;
        return this._dbObject.equals(rhs._dbObject);
	}

	@Override
	public int hashCode() {
		return _dbObject.hashCode();
	}

	@Override
	public String toString() {
		return _dbObject.toString();
	}
	
	public DBObject graphId(){

		ObjectId oid = (ObjectId) this._dbObject.get(ID_KEY);
		BasicDBObject typedId = new BasicDBObject(ID_KEY, oid);
		
		// Add the optional type
		String type = getType();
		if(type != null)
			typedId.append(TYPE_KEY, type);
		
		return typedId;		
	}
	
	protected abstract String getType();
}
