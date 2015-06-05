package org.networklibrary.neonetworkanalyzer.statistics;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.neo4j.graphalgo.CostAccumulator;
import org.neo4j.graphalgo.impl.centrality.ShortestPathBasedCentrality;
import org.neo4j.graphalgo.impl.shortestpath.SingleSourceShortestPath;
import org.neo4j.graphdb.Node;

public class StressCentrality<ShortestPathCostType> extends
ShortestPathBasedCentrality<Long, ShortestPathCostType> {

	public StressCentrality(
			SingleSourceShortestPath<ShortestPathCostType> singleSourceShortestPath,
			Set<Node> nodeSet )
	{
		super( singleSourceShortestPath, new CostAccumulator<Long>(){

			@Override
			public Long addCosts(Long c1, Long c2) {
				return c1 + c2;
			}
			
		}, 0L, nodeSet );
	}

	@Override
	public void processShortestPaths(
			Node node,
			SingleSourceShortestPath<ShortestPathCostType> singleSourceShortestPath) {

		for(Node n : nodeSet){
			Set<List<Node>> paths = new HashSet<List<Node>>(singleSourceShortestPath.getPathsAsNodes(n));
//			List<List<Node>> paths = singleSourceShortestPath.getPathsAsNodes(n);
			
			for(List<Node> path : paths){
				for(int i = 1; i < (path.size()-1); ++i){
					addCentralityToNode(path.get(i), 1L);
				}
			}

		}

	}
//	public void processShortestPaths(
//			Node node,
//			SingleSourceShortestPath<ShortestPathCostType> singleSourceShortestPath) {
//
//		Map<Node,List<Relationship>> predecessors = singleSourceShortestPath.getPredecessors();
//		Map<Node,List<Relationship>> successors = Util.reversedPredecessors( predecessors );
//
//		
//		
//		for(Node target : nodeSet){
//			Queue<Node> q = new LinkedList<Node>();
//			
//			q.add(target);
//			
//			while(!q.isEmpty()){
//				Node n = q.poll();
//				
//				List<Relationship> next = predecessors.get(n);
//				if(next == null || next.size() == 0)
//					continue;
//				
//				if(!n.equals(node) && !n.equals(node)){
////					addCentralityToNode(n, (double)next.size());
//					addCentralityToNode(n, 1.0);
//				}
//					
//				for(Relationship rel : next){
//					Node other = rel.getOtherNode(n);
//					
//					q.add(other);
//				}
//					
//			}
//		}
		
		

//		for(Node n : nodeSet){
////			addCentralityToNode(n, 1.0);
//			List<Relationship> rels = successors.get(n);
//			
//			if(rels != null){
//				for(Relationship rel : rels){
//					Node other = rel.getOtherNode(n);
//					
//					if(!other.equals(node))	
//						addCentralityToNode(other, 1.0);
//					
//				}
//			}
//		}
//}


	

}
