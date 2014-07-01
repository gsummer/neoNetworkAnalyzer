package org.networklibrary.neonetworkanalyzer.neo4jalgos;

import org.neo4j.graphdb.Node;

public class Radiality<ShortestPathCostType> {

	protected int diameter = 0;
	protected AverageShortestPath<Integer> avgSP;
	
	public Radiality(int diameter,AverageShortestPath<Integer> avgSP){
		this.diameter = diameter;
		this.avgSP = avgSP;
	}
	
	public double calcRadiality(Node node){
		double asp = avgSP.getCentrality(node);
		double rad = (diameter + 1.0 - asp) / diameter;
		return rad;
	}
	
}
