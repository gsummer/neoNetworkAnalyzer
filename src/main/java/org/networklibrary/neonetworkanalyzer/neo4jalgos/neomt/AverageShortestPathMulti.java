package org.networklibrary.neonetworkanalyzer.neo4jalgos.neomt;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.neo4j.graphalgo.impl.centrality.ShortestPathBasedCentrality;
import org.neo4j.graphalgo.impl.shortestpath.SingleSourceShortestPath;
import org.neo4j.graphalgo.impl.util.DoubleAdder;
import org.neo4j.graphdb.Node;
import org.networklibrary.neonetworkanalyzer.neo4jalgos.cymt.MultiUtils;

public class AverageShortestPathMulti<ShortestPathCostType> extends
ShortestPathBasedCentrality<Double, ShortestPathCostType> {

	public AverageShortestPathMulti(
			SingleSourceShortestPath<ShortestPathCostType> singleSourceShortestPath,
			Set<Node> nodeSet) {
		super(singleSourceShortestPath, new DoubleAdder(), 0.0, nodeSet);
	}

	protected Map<Node,Long> numPathsMap = new HashMap<Node,Long>();
	protected Map<Node,Long> sumPathsMap = new HashMap<Node,Long>();

	@Override
	public void processShortestPaths(
			Node node,
			SingleSourceShortestPath<ShortestPathCostType> singleSourceShortestPath) {

		long sum = 0;
		long numPaths = 0;

		Set<Node> visited = new HashSet<Node>();

		for(Node n : nodeSet){
			List<List<Node>> paths = singleSourceShortestPath.getPathsAsNodes(n);
			//			numPaths += paths.size();

			for(List<Node> path : paths){

				for(int i = 1; i < path.size(); ++i){
					if(visited.contains(path.get(i)))
						continue;
					else {
						sum += i;
						++numPaths;
						visited.add(path.get(i));
					}
				}
			}
		}

		//		double avgSP = sum / (double)numPaths;
		//		setCentralityForNode(node, avgSP);
		//		numPaths

		numPathsMap.put(node,new Long(numPaths));
		sumPathsMap.put(node, new Long(sum));

	}

	@Override
	public Double getCentrality( Node node )
	{
		calculate();
		return centralities.get( sumPathsMap.get(node) / (numPathsMap.get(node).doubleValue()) );
	}
	
	public void addNumPaths(Node n, long num){
		MultiUtils.addMapped(n, num, numPathsMap);
	}
	
	public void addSumPaths(Node n, long sum){
		MultiUtils.addMapped(n, sum, sumPathsMap);
	}
	
	public long getNumPaths(Node n){
		return numPathsMap.get(n);
	}
	
	public long getSumPaths(Node n){
		return sumPathsMap.get(n);
	}

}
