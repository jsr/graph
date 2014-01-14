package com.mongodb.graph;

import java.util.Collection;
import java.util.List;
import com.mongodb.DBObject;

/**
 * An Interface for working with Graph Data in MongoDB irrespective 
 * of the specific model that is used to store it. The interface
 * provides common operations for building a data graph and performing
 * various types of typical graph queries.
 *
 */
public interface GraphEngine{

	/**
	 * Add a directed edge to the graph. This creates a relationship
	 * from the source object to the destination with the given edge 
	 * data.
	 * 
	 * @param sourceId the logical identifier of the source vertex
	 * for the new edge. This can be a document of any type (or
	 * even a simple ObjectId), as long as the same document is always 
	 * used when referring to that vertex 
	 * @param destinationId the logical identifier for the destination
	 * or target end of the edge
	 * @param edgeData the data with which to annotate this edge. This data 
	 * can be any document and may be used to filter edges during queries.
	 * @return the edge document created internally for this edge. It includes
	 * both the edgeData and the source and destination identifiers. This 
	 * complete document is the one returned by any queries on the engine.
	 * 
	 */
	public DBObject addEdge(
			DBObject sourceId, DBObject destinationId,
			DBObject edgeData);
	
	/**
	 * Get all edges connected to the provided vertex identifier. This
	 * unfiltered version will return all edges both incoming and outgoing.
	 * @param vertexId the identifier of the target vertex. 
	 * @return returns any edges for which vertexId was supplied as the 
	 * source or destination.
	 */
	public Collection<DBObject> getEdges(DBObject vertexId);
	
	/**
	 * Get all edges connected to a given vertex which also match the 
	 * provided filter.
	 * @param vertexId the identifier of the target vertex. 
	 * @param filter the EdgeFilter instance that constrains which
	 * edges must be returned. The EdgeFilter may be used to constrain
	 * direction or filter by edge data and may also project which
	 * edge data should be returned.
	 * @return returns any edges for the vertexId that match the filter
	 */
	public Collection<DBObject> getEdges(
			DBObject vertexId, EdgeFilter filter);
	
	/**
	 * Get all neighboring vertices to the provided vertex identifier. This
	 * unfiltered version will return a relationship for all edges both 
	 * incoming and outgoing.
	 * @param vertexId the identifier of the target vertex. 
	 * @return returns one Relationship object for each edge associated with
	 * the identified vertex. A Relationship contains information about the
	 * connected vertex (neighbor) as well as the edge which connects it.
	 */
	public Collection<Relationship> getNeighbors(DBObject vertexId);
	
	/**
	 * Get all neighbors connected to a given vertex which also match the 
	 * provided filter.
	 * @param vertexId the identifier of the target vertex. 
	 * @param filter the EdgeFilter instance that constrains which
	 * relationships must be returned. The EdgeFilter may be used to constrain
	 * direction or filter by edge data and may also project which
	 * edge data should be returned.
	 * @return returns one Relationship object for each edge associated with
	 * the identified vertex which also matches the provided filter. A 
	 * Relationship contains information about the connected vertex (neighbor) 
	 * as well as the edge which connects it.
	 */
	public Collection<Relationship> getNeighbors(
			DBObject vertexId, EdgeFilter filter);
	
	/**
	 * Find paths between two specified vertices in the graph. The graph is 
	 * traversed to return paths consisting of multiple degrees. This may be
	 * used to discover how vertices are connected even if they are not direct
	 * neighbors in the graph. 
	 * NOTE : By default this is a directed search and therefore ONLY outgoing
	 * edges of any given node are investigated. This behavior can be 
	 * overridden using an EdgeFilter which explicitly specifies that 
	 * edge direction may be Direction.BOTH.
	 * @param fromVertexId the vertex from which traversal begins
	 * @param targetVertexId the vertex to which the paths are being found
	 * @param filter an EdgeFilter which may be used to constrain the edges
	 * investigated during the search. If null, then default behavior is a 
	 * directed search of all outgoing edges unfiltered by value.
	 * @param degreeLimit the limit of number of degrees to perform the search.
	 * Only paths of this degree or less will be returned.
	 * @param listener a delegate that can receive paths as they are discovered.
	 * @return a collection of all discovered paths between the nodes which match
	 * the filter criteria and degree limit.
	 */
	public Collection<List<Relationship>> findPaths(
			DBObject fromVertexId, DBObject targetVertexId, 
			EdgeFilter filter, int degreeLimit, PathListener listener);
}