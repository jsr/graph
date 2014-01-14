package com.mongodb.graph;

import java.util.List;

/**
 * 	Interface may be implemented by a user and used to receive paths
 * 	from calls to GraphEngine.findPaths(). Paths will be fired to 
 * 	handlePath as they are discovered by the traversal.
 */
public interface PathListener {
	
	/**
	 * Implement this method to receive paths as they are discovered
	 * @param path the discovered path represented as a List of Relationships
	 */
	void handlePath(List<Relationship> path);
	
	/**
	 * Called by the query when it is complete
	 */
	void complete();
}
