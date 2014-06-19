package org.networklibrary.neonetworkanalyzer.neo4jalgos;

import java.util.HashSet;
import java.util.Set;

import org.neo4j.graphdb.Node;

public class TopologicalCoeff {

	public double calcTopologicalCoeff(Node node){
		Set<Node> neighbours = NetworkUtils.getUniqueNeighbours(node);
		
		if(neighbours.size() < 2)
			return 0.0;
		
		int tc = 0;
		
		Set<Node> comNeNodes = new HashSet<Node>();
		for (final Node nb : neighbours) {
			Set<Node> currentComNeNodes = NetworkUtils.getUniqueNeighbours(nb);
			for (final Node n : currentComNeNodes) {
				if (n != node) {
					tc++;
					if (comNeNodes.add(n)) {
						if (neighbours.contains(n)) {
							tc++;
						}
					}
				}
			}
		}
		
		return (double) tc / (double) (comNeNodes.size() * neighbours.size());
	}
}
