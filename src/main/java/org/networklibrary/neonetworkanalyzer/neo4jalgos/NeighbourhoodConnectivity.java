package org.networklibrary.neonetworkanalyzer.neo4jalgos;

import java.util.Set;

import org.neo4j.graphdb.Node;

public class NeighbourhoodConnectivity {

	public double calcNeighbourhoodConnectivity(Node node){
		
		Set<Node> neighbours = NetworkUtils.getUniqueNeighbours(node);
		
		long neighbourSum = 0;
		
		for(Node neighbour : neighbours){
			neighbourSum += NetworkUtils.getUniqueNeighbours(neighbour).size();
		}
		
		double neighbourConnectivity = (double)neighbourSum / (double)neighbours.size();
		
		return neighbourConnectivity;
	}
	
}
