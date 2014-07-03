package org.networklibrary.neonetworkanalyzer.neo4jalgos;

import java.util.HashSet;
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
	
}
