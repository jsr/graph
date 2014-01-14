package com.mongodb.graph;

import com.mongodb.DBObject;

/**
 * 	Represents a relationship or hop in a data graph. 
 * 	All Relationship instances are relative to some reference vertex. 
 * 	For example when they are used to describe a neighbor they are relative 
 * 	to vertex for which the neighbor query was performed. When presented in a 
 * 	list as a path, each one is relative to the previous in the list.
 */
public class Relationship {	

	private DBObject _relatedId;
	private DBObject _edge;
	private Direction _direction;

	public Relationship(DBObject relatedId, DBObject edge, Direction direction) {
		super();
		this._relatedId = relatedId;
		this._edge = edge;
		this._direction = direction;
	}
	
	public DBObject getRelatedId() {
		return _relatedId;
	}

	public Direction getDirection() {
		return _direction;
	}

	public DBObject getEdge() {
		return _edge;
	}

	public boolean isIncoming(){
		return this._direction != Direction.OUT;
	}
	
	public boolean isOutgoing(){
		return this._direction != Direction.IN;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((_direction == null) ? 0 : _direction.hashCode());
		result = prime * result + ((_edge == null) ? 0 : _edge.hashCode());
		result = prime * result
				+ ((_relatedId == null) ? 0 : _relatedId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Relationship other = (Relationship) obj;
		if (_direction != other._direction)
			return false;
		if (_edge == null) {
			if (other._edge != null)
				return false;
		} else if (!_edge.equals(other._edge))
			return false;
		if (_relatedId == null) {
			if (other._relatedId != null)
				return false;
		} else if (!_relatedId.equals(other._relatedId))
			return false;
		return true;
	}	
}
