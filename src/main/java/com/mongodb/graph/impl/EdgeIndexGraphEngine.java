package com.mongodb.graph.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import com.mongodb.*;
import com.mongodb.graph.Direction;
import com.mongodb.graph.EdgeFilter;
import com.mongodb.graph.GraphEngine;
import com.mongodb.graph.PathListener;
import com.mongodb.graph.Relationship;

public class EdgeIndexGraphEngine implements GraphEngine 
{
	private static final int DEFAULT_BATCH_SIZE = 100;
	private static final int DEFAULT_RESULT_LIMIT = 1000;
	private static final String DEFAULT_EDGE_COLLECTION = "graph_edges";
	private static final String SOURCE_KEY = "_s";
	private static final String DEST_KEY = "_d";	
		
	private DB _db = null;
	private DBCollection _edgeColl = null;
	
	public EdgeIndexGraphEngine(DB db){
		
		_db = db;
		_edgeColl = _db.getCollection(DEFAULT_EDGE_COLLECTION);	
		
        this._edgeColl.ensureIndex(
                new BasicDBObject(SOURCE_KEY, 1).append(DEST_KEY, 1));
        this._edgeColl.ensureIndex(
                new BasicDBObject(DEST_KEY, 1).append(SOURCE_KEY, 1));	
	}

	@Override
	public DBObject addEdge(DBObject sourceVertexId, DBObject targetVertexId, DBObject edgeData) {
		
		BasicDBObject newEdge = new BasicDBObject();
		newEdge.putAll(edgeData);
		newEdge.append(SOURCE_KEY, sourceVertexId);
		newEdge.append(DEST_KEY, targetVertexId);				
		this._edgeColl.insert(newEdge);		
		
		return newEdge;
	}
	
	@Override
	public List<DBObject> getEdges(DBObject vertexId){
		
		return this.getEdges(vertexId, null);
	}
		
	@Override
	public List<DBObject> getEdges(DBObject vertexId, EdgeFilter filter){
		
		// Setup the query components based on filter
		Direction direction = getAllowedDirection(filter, Direction.BOTH);
		BasicDBObject projection = buildProjection(filter);	
		BasicDBObject edgeQuery = buildEdgeQuery(vertexId, filter, direction);
			
		// In this call, we just need the edges as documents
		List<DBObject> resultList = new ArrayList<DBObject>();
		
		DBCursor outCursor = null;
		try{
			// run query and exhaust cursor
			outCursor = _edgeColl.find(edgeQuery, projection);
			outCursor.batchSize(DEFAULT_BATCH_SIZE);
			outCursor.limit(DEFAULT_RESULT_LIMIT);
			
			// exhaust cursor
			while(outCursor.hasNext())
				resultList.add(outCursor.next());
		}
		finally{
			if(outCursor != null)
				outCursor.close();
		}

		return resultList;		
	}
	
	@Override
	public List<Relationship> getNeighbors(DBObject vertexId){
		return getNeighbors(vertexId, null);
	}
	
	@Override
	public List<Relationship> getNeighbors(DBObject vertexId, EdgeFilter filter){
		
		// Find all edges by default
		Direction direction = getAllowedDirection(filter, Direction.BOTH);
		BasicDBObject projection = buildProjection(filter);	
		BasicDBObject edgeQuery = buildEdgeQuery(vertexId, filter, direction);
			
		// In this call, we need to construct relationships
		List<Relationship> resultList = new ArrayList<Relationship>();
		
		DBCursor outCursor = null;
		try{
			// run query and exhaust cursor
			outCursor = _edgeColl.find(edgeQuery, projection);
			outCursor.batchSize(DEFAULT_BATCH_SIZE);
			outCursor.limit(DEFAULT_RESULT_LIMIT);
			
			// exhaust cursor
			while(outCursor.hasNext())
				resultList.add(relationshipFromEdge(outCursor.next(), vertexId));
		}
		finally{
			if(outCursor != null)
				outCursor.close();
		}
		
		return resultList;		
	}
	
	@Override
	public Collection<List<Relationship>> findPaths(
			DBObject fromVertexId, DBObject targetVertexId, 
			EdgeFilter filter, int degreeLimit, PathListener listener){
		
		List<List<Relationship>> resultList = new LinkedList<List<Relationship>>();
		List<Relationship> currentPath = new ArrayList<Relationship>(degreeLimit);
		
		// Add the id for the start into the current path
		processLevel(fromVertexId, fromVertexId, targetVertexId, filter,  
				degreeLimit, currentPath, resultList, listener);
		if(listener != null) listener.complete();
		return resultList;
	}

	private void processLevel(
			DBObject startVertex, DBObject levelVertexId, DBObject targetVertexId, 
			EdgeFilter filter, int degrees, List<Relationship> currentPath,
			List<List<Relationship>> resultList, PathListener listener){
		
		if(degrees > 0 && levelVertexId != null && targetVertexId != null){	
			
			// Find all edges by default
			Direction direction = getAllowedDirection(filter, Direction.OUT);
			BasicDBObject projection = buildProjection(filter);	
			BasicDBObject edgeQuery = buildEdgeQuery(levelVertexId, filter, direction);
							
			DBCursor outCursor = null;
			try{
				// run query and exhaust cursor
				outCursor = _edgeColl.find(edgeQuery, projection);
				outCursor.batchSize(DEFAULT_BATCH_SIZE);
				outCursor.limit(DEFAULT_RESULT_LIMIT);
				
				// exhaust cursor
				while(outCursor.hasNext()){
					Relationship current = relationshipFromEdge(
							outCursor.next(), levelVertexId);
					
					if(pathHasVertex(startVertex, currentPath, 
							current.getRelatedId()) == false){
						
						// push the current hop to the path
						currentPath.add(current);
						
						if(idMatch(targetVertexId, current.getRelatedId())){
							// This is a result add it !
							ArrayList<Relationship> resultPath = new ArrayList<Relationship>(currentPath);
							resultList.add(resultPath);
							if(listener != null) listener.handlePath(resultPath);
						}
						else{
							// This is a path to investigate
							processLevel(startVertex, current.getRelatedId(), targetVertexId, filter, 
									degrees - 1, currentPath, resultList, listener);
						}
					
						// pop the current hop off 
						currentPath.remove(currentPath.size() - 1);
					}
				}
			}
			finally{
				if(outCursor != null)
					outCursor.close();
			}
		}		
	}

	private boolean pathHasVertex(DBObject startVertex, 
			List<Relationship> currentPath, DBObject relatedId) {
		for(Relationship step : currentPath){
			if(step.getRelatedId().equals(relatedId))
				return true;
		}
		
		return startVertex.equals(relatedId);
	}

	private Relationship relationshipFromEdge(DBObject edge, DBObject relativeTo) {
		DBObject relatedId = (DBObject) edge.get(SOURCE_KEY);
		Direction edgeDirection = Direction.IN;
		
		// if source == relativeTo, then this is an outgoing
		if(relatedId.equals(relativeTo)){
			relatedId = (DBObject) edge.get(DEST_KEY);
			edgeDirection = Direction.OUT;
		}

		return new Relationship(relatedId, edge, edgeDirection);		
	}

	private BasicDBObject buildEdgeQuery(DBObject vertexId, 
			EdgeFilter filter, Direction direction) {
		
		// Initialize query with terms for direction
		BasicDBObject edgeQuery = new BasicDBObject();
			
		// Based on the allowed direction, add a clause to 
		// specify the source/destination we expect
		if(direction == Direction.BOTH ){
			List<BasicDBObject> orList = new ArrayList<BasicDBObject>(2);
			orList.add(new BasicDBObject(SOURCE_KEY, vertexId));
			orList.add(new BasicDBObject(DEST_KEY, vertexId));			
			edgeQuery.append("$or", orList);
		} 
		else if(direction == Direction.OUT){
			edgeQuery.append(SOURCE_KEY, vertexId);			
		}
		else {// direction == Direction.IN 
			edgeQuery.append(DEST_KEY, vertexId);			
		}
		
		if(filter != null && filter.getQuery() != null){
			// need explicit $and to avoid $or clashes in user query
			List<DBObject> andList = new ArrayList<DBObject>(2);
			andList.add(edgeQuery);
			andList.add(filter.getQuery());
	    	edgeQuery = new BasicDBObject("$and", andList);  
		}
			
		return edgeQuery;
	}

	
	private BasicDBObject buildProjection(EdgeFilter filter) {

		BasicDBObject projection = null;	
		if(filter != null && filter.hasProjection()){
			projection = new BasicDBObject(SOURCE_KEY, true).append(DEST_KEY, true);
			projection.putAll(filter.getProjection());
		}
		
		return projection;
	}


	private Direction getAllowedDirection(
			EdgeFilter filter, Direction defaultDirection) {
		return (filter == null || filter.getAllowedDirection() == null) ? 
				defaultDirection : filter.getAllowedDirection();
	}
		
	private boolean idMatch(DBObject original, DBObject subject) {
		return original.equals(subject);
	}
}