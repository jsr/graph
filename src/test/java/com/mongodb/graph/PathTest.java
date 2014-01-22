package com.mongodb.graph;

import static org.junit.Assert.*;
import static com.mongodb.graph.test.utils.FilteredDBObject.*;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import com.mongodb.BasicDBObject;
import com.mongodb.graph.test.utils.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

@RunWith(Parameterized.class)
public class PathTest extends GraphTestBase{

    public PathTest(String testName, Class<? extends GraphEngine> impl) 
            throws Exception {
        
    	startEngine(testName, PathTest.class.getSimpleName(), impl);
    }
    
    @Test
    public void testFindPathsNonExistent() throws Exception {
    	
    	// get the edges for a vertex that doesnt exist
		PersonVertex ghost1 = new PersonVertex("Nobody");
		PersonVertex ghost2 = new PersonVertex("AlsoNobody");
		Collection<List<Relationship>> result = 
				engine.findPaths(ghost1.graphId(), bob.graphId(), null, 10, null);
		assertEquals(0, result.size());

		result = engine.findPaths(bob.graphId(), ghost2.graphId(), null, 10, null);
		assertEquals(0, result.size());

		result = engine.findPaths(ghost1.graphId(), ghost2.graphId(), null, 10, null);
		assertEquals(0, result.size());
    }

    @Test
    public void testFindPathsUnfiltered() throws Exception {
    	
		Collection<List<Relationship>> result = null;
				
		// Nothing directional (OUT only) from alice to sam
		result = engine.findPaths(alice.graphId(), sam.graphId(), null, 10, null);
		assertEquals(0, result.size());
    	
		// alice->father->bob
		result = engine.findPaths(alice.graphId(), bob.graphId(), null, 10, null);
		List<Relationship> correctPath = new ArrayList<Relationship>();
		correctPath.add(new Relationship(bob.graphId(), withoutId(alice_bob), Direction.OUT));		
		assertEquals(1, result.size());
		assertTrue(result.contains(correctPath));
    	
		// alice->father->bob->workedat->ford
		result = engine.findPaths(alice.graphId(), ford.graphId(), null, 10, null);
		correctPath.add(new Relationship(ford.graphId(), withoutId(bob_ford), Direction.OUT));		
		assertEquals(1, result.size());
		assertTrue(result.contains(correctPath));
   }

    @Test
    public void testFindPathsDirectionFiltered() throws Exception {
    	
		EdgeFilter bothFilter = new EdgeFilter(Direction.BOTH);
    	Collection<List<Relationship>> result = null;
				    	
		// Not enough hops to find 3 degree
		result = engine.findPaths(alice.graphId(), sam.graphId(), bothFilter, 2, null);
		assertEquals(0, result.size());
    	    	
		List<Relationship> correctPath = new ArrayList<Relationship>();
		correctPath.add(new Relationship(bob.graphId(), withoutId(alice_bob), Direction.OUT));		
		
		// alice->father->bob
		result = engine.findPaths(alice.graphId(), bob.graphId(), bothFilter, 10, null);
		assertEquals(1, result.size());
		assertTrue(result.contains(correctPath));
    	
		// alice->father->bob->workedat->ford
		result = engine.findPaths(alice.graphId(), ford.graphId(), bothFilter, 10, null);
		correctPath.add(new Relationship(ford.graphId(), withoutId(bob_ford), Direction.OUT));		
		assertEquals(1, result.size());
		assertTrue(result.contains(correctPath));
		
		// alice->father->bob->workedat->ford<-workedat<-sam
		result = engine.findPaths(alice.graphId(), sam.graphId(), bothFilter, 3, null);
		correctPath.add(new Relationship(sam.graphId(), withoutId(sam_ford), Direction.IN));		
		assertEquals(1, result.size());
		assertTrue(result.contains(correctPath));		
   }

    @Test
    public void testFindPathsDataFiltered() throws Exception {
    	
    	Collection<List<Relationship>> result = null;
    	
    	// Run with an edge filter that will stop the path
    	BasicDBObject failquery = new BasicDBObject("start_date", 
    			new BasicDBObject("$lt", new Date(250)));    	
		EdgeFilter failFilter = new EdgeFilter(Direction.BOTH, failquery, null);
		
		result = engine.findPaths(alice.graphId(), ford.graphId(), failFilter, 10, null);
		assertEquals(0, result.size());		
		
    	// Run again with filter that will find the path
		List<BasicDBObject> orList = new ArrayList<BasicDBObject>(2);
		orList.add(new BasicDBObject("start_date", new BasicDBObject("$gt", new Date(300))));
		orList.add(new BasicDBObject("end_date", new BasicDBObject("$exists", false)));
    	BasicDBObject findquery = new BasicDBObject("$or", orList);  
		
		EdgeFilter findFilter = new EdgeFilter(Direction.BOTH, findquery, null);
		
		List<Relationship> correctPath = new ArrayList<Relationship>();
		correctPath.add(new Relationship(bob.graphId(), withoutId(alice_bob), Direction.OUT));		
		correctPath.add(new Relationship(ford.graphId(), withoutId(bob_ford), Direction.OUT));		
		
		// alice->father->bob->workedat->ford
		result = engine.findPaths(alice.graphId(), ford.graphId(), findFilter, 10, null);
		assertEquals(1, result.size());
		assertTrue(result.contains(correctPath));		
   }

}
