package org.networklibrary.neonetworkanalyzer.neo4jalgos;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.neo4j.graphalgo.impl.centrality.ShortestPathBasedCentrality;
import org.neo4j.graphalgo.impl.shortestpath.SingleSourceShortestPath;
import org.neo4j.graphalgo.impl.util.DoubleAdder;
import org.neo4j.graphdb.Node;

public class StressCentrality<ShortestPathCostType> extends
		ShortestPathBasedCentrality<Double, ShortestPathCostType> {

	public StressCentrality(
	        SingleSourceShortestPath<ShortestPathCostType> singleSourceShortestPath,
	        Set<Node> nodeSet )
	    {
	        super( singleSourceShortestPath, new DoubleAdder(), 0.0, nodeSet );
	    }
	
	@Override
	public void processShortestPaths(
			Node node,
			SingleSourceShortestPath<ShortestPathCostType> singleSourceShortestPath) {
				
		for(Node n : nodeSet){
			Set<List<Node>> paths = new HashSet<List<Node>>(singleSourceShortestPath.getPathsAsNodes(n));
			
			for(List<Node> path : paths){
				for(int i = 1; i < (path.size()-1); ++i){
					addCentralityToNode(path.get(i), 1.0);
				}
			}
			
		}
		
	}

}
