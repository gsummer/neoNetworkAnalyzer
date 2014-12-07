package org.networklibrary.neonetworkanalyzer.neo4jalgos;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

public class NetworkUtils {

	static public Set<Node> getUniqueNeighbours(Node n){
		Set<Node> uniqueNeighbours = new HashSet<Node>();
		
		// that really correct? more testing needed
		for(Relationship rel : n.getRelationships()){
			if(!rel.getOtherNode(n).equals(n))
				uniqueNeighbours.add(rel.getOtherNode(n));
		}
		
		return uniqueNeighbours;
	}
		
	static public List<Relationship> getConnectingEdges(Node a, Node b){
		
		List<Relationship> results = new ArrayList<Relationship>();
		
		for(Relationship r : a.getRelationships()){
			if(r.getOtherNode(a).equals(b)){
				results.add(r);
			}
		}
		
		return results;
	}
}
