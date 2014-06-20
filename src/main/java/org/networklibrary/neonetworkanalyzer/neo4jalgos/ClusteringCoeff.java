package org.networklibrary.neonetworkanalyzer.neo4jalgos;

import java.util.Set;

import org.neo4j.graphdb.Node;

public class ClusteringCoeff {

	public double calcClusteringCoeff(Node node){
		
		double clustCoeff = 0.0;
		
		Set<Node> neighbourhood = NetworkUtils.getUniqueNeighbours(node);
		
		long edgeCount = 0;
		long neighbourhoodSize = neighbourhood.size();
		
		for(Node neighbour : neighbourhood){
			Set<Node> intersect = NetworkUtils.getUniqueNeighbours(neighbour);
			intersect.retainAll(neighbourhood);
			edgeCount += intersect.size();
		}
		
		// edges are counted twice because of the intersect
		edgeCount /= 2;
		
		clustCoeff = (double) 2*edgeCount / (neighbourhoodSize * (neighbourhoodSize-1));
		
		if(Double.isNaN(clustCoeff))
			return 0.0;
		else
			return clustCoeff;
	}
	
	
}
