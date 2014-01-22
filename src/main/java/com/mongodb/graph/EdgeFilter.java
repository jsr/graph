package com.mongodb.graph;

import com.mongodb.DBObject;

/**
 *	Used to specify rules for filtering edges that are returned
 *	or traversed during graph query operations. Can also be used
 *	to project/constrain the edge data that is retrieved as part
 *	of the query.
 */
public class EdgeFilter {
	
	private final Direction _allowedDirection;
	private final DBObject _query;
	private final DBObject _projection;
	private boolean _includeIds = false;

	
	/**
	 * Construct an EdgeFilter which only constrains edge direction. 
	 * @param allowedDirection the allowed direction(s) for edges to
	 * be considered as part of the query.
	 */
	public EdgeFilter(Direction allowedDirection) {
		this._allowedDirection = allowedDirection;
		this._query = null;
		this._projection = null;		
	}

	/**
	 * Construct an EdgeFilter which may constrain by both edge
	 * direction and edge data. 
	 * @param allowedDirection the allowed direction(s) for edges to
	 * be considered as part of the query.
	 * @param query an expression formed as a MongoDB query that will be
	 * used to filter edges by data values. If null, then this field has
	 * no effect.
	 * @param projection an expression in the form of a MongoDB projection
	 * that will be used to limit the return information about an edge. If
	 * null, then no projection will be applied. 
	 */
	public EdgeFilter(
			Direction allowedDirection, 
			DBObject query,
			DBObject projection) {
		
		this._allowedDirection = allowedDirection;
		this._query = query;
		this._projection = projection;
	}

	public Direction getAllowedDirection() {
		return _allowedDirection;
	}

	public DBObject getQuery() {
		return _query;
	}

	public DBObject getProjection() {
		return _projection;
	}

	public boolean hasProjection() {
		return _projection != null;
	}

	/**
	 * Returns true if this filter allows edge IDs to be included in query results.
	 * Edge IDs are not always required and there is a performance cost to including
	 * them. By default this value is set to false.
	 * @return true if edge IDs are included
	 */
	public boolean getIncludeEdgeIds() {
		return _includeIds ;
	}

	/**
	 * Sets whether this filter allows edge IDs to be included in query results.
	 * Edge IDs are not always required and there is a performance cost to including
	 * them. By default this value is set to false.
	 */
	public void setIncludeEdgeIds(boolean includeIds) {
		this._includeIds = includeIds ;
	}

}
