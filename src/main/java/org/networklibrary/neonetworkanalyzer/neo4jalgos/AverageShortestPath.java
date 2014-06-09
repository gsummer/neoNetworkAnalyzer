package org.networklibrary.neonetworkanalyzer.neo4jalgos;

import java.util.List;
import java.util.Set;

import org.neo4j.graphalgo.impl.centrality.ShortestPathBasedCentrality;
import org.neo4j.graphalgo.impl.shortestpath.SingleSourceShortestPath;
import org.neo4j.graphalgo.impl.util.DoubleAdder;
import org.neo4j.graphdb.Node;

public class AverageShortestPath<ShortestPathCostType> extends
		ShortestPathBasedCentrality<Double, ShortestPathCostType> {

	public AverageShortestPath(
			SingleSourceShortestPath<ShortestPathCostType> singleSourceShortestPath,
			Set<Node> nodeSet) {
		super(singleSourceShortestPath, new DoubleAdder(), 0.0, nodeSet);
	}

	@Override
	public void processShortestPaths(
			Node node,
			SingleSourceShortestPath<ShortestPathCostType> singleSourceShortestPath) {
		
		long sum = 0;
		long numPaths = 0;
		
		for(Node n : nodeSet){
			List<List<Node>> paths = singleSourceShortestPath.getPathsAsNodes(n);
			numPaths += paths.size();
			for(List<Node> path : paths){
				sum += path.size();
			}
		}
		
		double avgSP = sum / (double)numPaths;
		setCentralityForNode(node, avgSP);
		
	}

}
