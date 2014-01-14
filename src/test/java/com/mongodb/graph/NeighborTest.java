package com.mongodb.graph;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.graph.test.utils.*;

import java.util.Collection;
import java.util.Date;

@RunWith(Parameterized.class)
public class NeighborTest extends GraphTestBase{

    public NeighborTest(String testName, Class<? extends GraphEngine> impl) 
            throws Exception {
    	
    	startEngine(testName, NeighborTest.class.getSimpleName(), impl);
    }
    

	@Test
    public void testGetEdgesNonExistent() throws Exception {
    	
    	// get the edges for a vertex that doesnt exist
		PersonVertex ghost = new PersonVertex("Nobody");
		Collection<DBObject> result = engine.getEdges(ghost.graphId());
		assertEquals(0, result.size());
   }

    @Test
    public void testGetEdgesUnfiltered() throws Exception {
    	
		Collection<DBObject> result = engine.getEdges(bob.graphId());
		assertEquals(3, result.size());
		assertTrue(result.contains(bob_acme));
		assertTrue(result.contains(bob_ford));
		assertTrue(result.contains(alice_bob));
    	
		result = engine.getEdges(sam.graphId());
		assertEquals(1, result.size());
		assertTrue(result.contains(sam_ford));
    	
		result = engine.getEdges(alice.graphId());
		assertEquals(1, result.size());
		assertTrue(result.contains(alice_bob));
   }

    @Test
    public void testGetEdgesDirectionFilter() throws Exception {
    	
		Collection<DBObject> result = engine.getEdges(
				bob.graphId(), new EdgeFilter(Direction.BOTH));
		assertEquals(3, result.size());
		assertTrue(result.contains(bob_acme));
		assertTrue(result.contains(bob_ford));
		assertTrue(result.contains(alice_bob));
    	
		result = engine.getEdges(
				bob.graphId(), new EdgeFilter(Direction.OUT));
		assertEquals(2, result.size());
		assertTrue(result.contains(bob_acme));
		assertTrue(result.contains(bob_ford));
		assertFalse(result.contains(alice_bob));
    	
		result = engine.getEdges(
				bob.graphId(), new EdgeFilter(Direction.IN));
		assertEquals(1, result.size());
		assertFalse(result.contains(bob_acme));
		assertFalse(result.contains(bob_ford));
		assertTrue(result.contains(alice_bob));
   }

    @Test
    public void testGetEdgesQueryFilter() throws Exception { 	
		
    	BasicDBObject query = new BasicDBObject("start_date", 
    			new BasicDBObject("$lt", new Date(250)));
    	
    	Collection<DBObject> result = engine.getEdges(
				bob.graphId(), new EdgeFilter(Direction.BOTH, query, null));
		assertEquals(1, result.size());
		assertTrue(result.contains(bob_acme));
    	
    	result = engine.getEdges(
				bob.graphId(), new EdgeFilter(Direction.OUT, query, null));
		assertEquals(1, result.size());
		assertTrue(result.contains(bob_acme));
    	
    	result = engine.getEdges(
				bob.graphId(), new EdgeFilter(Direction.IN, query, null));
		assertEquals(0, result.size());
   }
}
