package org.networklibrary.neonetworkanalyzer.statistics;

import java.util.HashSet;
import java.util.Set;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

public class MultiEdgePairs {

	public long calcMultipleEdgePairs(Node node){
		
		long pairs = 0;
		Set<Node> visited = new HashSet<Node>();
		for(Relationship rel : node.getRelationships()){
			Node other = rel.getOtherNode(node);
			if(visited.contains(other))
				++pairs;
			
			visited.add(other);
		}
		
		return pairs;
	}
}
