package com.mongodb.graph;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;

import org.junit.Before;
import org.junit.runners.Parameterized.Parameters;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.graph.impl.EdgeIndexGraphEngine;
import com.mongodb.graph.test.utils.CompanyVertex;
import com.mongodb.graph.test.utils.FamilyEdge;
import com.mongodb.graph.test.utils.PersonVertex;
import com.mongodb.graph.test.utils.WorksForEdge;

public class GraphTestBase {
	protected static final String BASE_URI = "mongodb://localhost/";
    protected static final String ID_KEY = "_id";
    
    protected DB database;
    protected GraphEngine engine;
	
    protected PersonVertex bob = new PersonVertex("Bob");
    protected PersonVertex alice = new PersonVertex("Alice");;
    protected PersonVertex sam = new PersonVertex("Sam");;

    protected CompanyVertex acme = new CompanyVertex("Acme");
    protected CompanyVertex ford = new CompanyVertex("Ford");
	
    protected DBObject bob_acme = null; 
    protected DBObject bob_ford = null; 
    protected DBObject sam_ford = null; 
    protected DBObject alice_bob = null; 

    protected void startEngine(String testName, String dbName,
    		Class<? extends GraphEngine> impl) throws Exception {
        
        String databaseName = dbName + "_" + testName;
        MongoClientURI uri = new MongoClientURI(BASE_URI + databaseName);
        this.database = new MongoClient(uri).getDB(databaseName);
        database.dropDatabase();
        
        // Load the configured Engine implementation 
        engine = impl.getConstructor(DB.class).newInstance(database);
		
	}

    @Before
    public void setUp() throws Exception {
        
        // Insert the people 
        DBCollection people = database.getCollection("people");
        people.insert(bob.toDBObject());
        people.insert(alice.toDBObject());
        people.insert(sam.toDBObject());
        
        // And the companies
        DBCollection company = database.getCollection("companies");
        company.insert(acme.toDBObject());
        company.insert(ford.toDBObject());       

        // Create a known network, bob worked for acme,
        // both bob and sam worked for ford
        bob_acme = engine.addEdge(bob.graphId(), acme.graphId(), 
        		new WorksForEdge(new Date(200), new Date(300)));
        bob_ford = engine.addEdge(bob.graphId(), ford.graphId(), 
        		new WorksForEdge(new Date(400), new Date(500)));
        sam_ford = engine.addEdge(sam.graphId(), ford.graphId(), 
        		new WorksForEdge(new Date(400), new Date(500)));
        
        // bob is alice's father
        alice_bob = engine.addEdge(alice.graphId(), bob.graphId(), 
        		new FamilyEdge("Father"));       
    }

    @Parameters
    public static Collection<Object[]> createInputValues() {
                        
        // Build the set of test params for the above configs           
        return Arrays.asList(new Object[][] {
            /*[0]*/ {"edgeindex", EdgeIndexGraphEngine.class}        
        });
    }

}
