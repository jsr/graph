mongodb-graphdata
=================

Framework for storing and querying graph data in MongoDB.

The project aims to define a uniform interface for creating and working with graph
data and a series of implementations that store the actual edge data in various forms.

The GraphEngine is essentially an edge store. It allows you to store vertex information
independently and refer to this data from the graph by Id. The framework
would also support adding graph connections to data already stored in MongoDB
and even other databases.

For example, assume you already have two MongoDB collections :

	> db.people.find().pretty()
	{ "_id" : ObjectId("52d4ad7da0ee474819b7b999"), "name" : "Bob" }
	{ "_id" : ObjectId("52d4ad7da0ee474819b7b99b"), "name" : "Sam" }
	
	> db.companies.find().pretty()
	{ "_id" : ObjectId("52d4ad7da0ee474819b7b99c"), "name" : "Acme" }
	{ "_id" : ObjectId("52d4ad7da0ee474819b7b99d"), "name" : "Ford" }
	
In the test code we use some simple Java helper classes to represent data objects. The 
code to create the collections above looks like :

    DBCollection people = db.getCollection("people");
    PersonVertex bob = new PersonVertex("Bob");
    PersonVertex sam = new PersonVertex("Sam");
    people.insert(bob.toDBObject());
    people.insert(sam.toDBObject());
    
    DBCollection companies = db.getCollection("companies");
    CompanyVertex acme = new CompanyVertex("Acme");
    CompanyVertex ford = new CompanyVertex("Ford");
    companies.insert(acme.toDBObject());
    companies.insert(ford.toDBObject());
    
The helpers have a toDBObject() method for creating a MongoDB document representation of
the instance and they also have a graphId() method which creates a small document with just 
enough information to identify the instance uniquely in the database (in this case, the graphId
contains the _id field and a type:person field so we know it can from that collection).

Building the Graph
------------------

In order to represent relationships between these entities we can use the GraphEngine interface 
as follows :

	GraphEngine graph = new EdgeIndexGraphEngine(db);
	
	graph.addEdge(bob.graphId(), ford.graphId(), 
		new BasicDBObject("type", "WorkedAt").append("title", "Engineer"));
	
	graph.addEdge(bob.graphId(), acme.graphId(), 
		new BasicDBObject("type", "WorkedAt").append("title", "Manager"));

For each edge, we give the engine an id for the source and destination vertices and then the
data for the edge itself. Here we are saying that Bob worked for both Ford and Acme in
different roles. The id document can be any valid MongoDB document as long as it is unique in
the system and the exact same document is used consistently when referring to any one vertex.

Finding Neighbors
-----------------

Now that we have a small graph, we can query. For example to see the information we just
added about bob's work history, we can simply do :

	Collection<Relationship> bobsNeighbors = graph.getNeighbors(bob.graphId());
	
This returns a Relationship object containing the edge and vertex information for each
edge we added above, however we can also filter this query. Lets say we only want to see
where bob worked as a manager, we can do the following :

	EdgeFilter managementOnly = new EdgeFilter(
		null, new BasicDBObject("title", "Manager"), null);
	
	Collection<Relationship> bobsManagement = 
		graph.getNeighbors(bob.graphId(), managementOnly);

The EdgeFilter may also be used to specify the direction of edges that may be returned. So
far all edges have bob as the source and therefore are OUTgoing, so the following filter
would not find any matches at all :

    EdgeFilter incomingOnly = new EdgeFilter(
		Direction.IN, new BasicDBObject("title", "Manager"), null);
    		
Finding Paths
-------------

Graphs are even more interesting over multiple degrees of separation. For example if we 
add the following edge to the same graph :

    graph.addEdge(sam.graphId(), acme.graphId(), 
		new BasicDBObject("type", "WorkedAt").append("title", "Accountant"));

Since the graph is small and simple, we can see that this means Bob and Sam both worked 
at Acme, but in large graphs this is not as easy to discover. The graph engine is able
to find paths over multiple degrees with the following call :

    Collection<List<Relationship>> bobAndSam = graph.findPaths(
		bob.graphId(), sam.graphId(), 
		new EdgeFilter(Direction.BOTH), 5, null);

This returns a single List<Relationship> showing the two hops that connect Bob and Sam 
through the Acme company, having searched over a maximum of 5 degrees (which of course this
sample graph does not yet have).		
		



