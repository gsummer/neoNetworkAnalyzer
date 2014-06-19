package org.networklibrary.neonetworkanalyzer.neo4jalgos;

import org.neo4j.graphdb.Node;

public class Radiality<ShortestPathCostType> {

	protected int diameter = 0;
	protected AverageShortestPath<ShortestPathCostType> asp;
	
	public Radiality(int diameter,AverageShortestPath<ShortestPathCostType> asp){
		this.diameter = diameter;
		this.asp = asp;
	}
	
	public double calcRadiality(Node node){
		double rad = (diameter + 1.0 - asp.getCentrality(node).doubleValue()) / diameter;
		return rad;
	}
	
}
