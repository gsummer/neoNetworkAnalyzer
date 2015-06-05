package org.networklibrary.neonetworkanalyzer.statistics;

import java.util.Map;

import org.neo4j.graphdb.Node;

public class RadialityMT<ShortestPathCostType> {

	protected long diameter = 0;
	protected Map<Node,Double> avgSP;
	
	public RadialityMT(long diameter,Map<Node,Double> avgSP){
		this.diameter = diameter;
		this.avgSP = avgSP;
	}
	
	public double calcRadiality(Node node){
		if(diameter == 0){
			return 0.0;
		}
		
		double asp = avgSP.get(node);
		double rad = (diameter + 1.0 - asp) / diameter;
		return rad;
	}
	
}
