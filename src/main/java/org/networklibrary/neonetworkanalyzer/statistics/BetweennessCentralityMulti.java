package org.networklibrary.neonetworkanalyzer.statistics;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.neo4j.graphalgo.impl.centrality.BetweennessCentrality;
import org.neo4j.graphalgo.impl.shortestpath.SingleSourceShortestPath;
import org.neo4j.graphalgo.impl.shortestpath.Util;
import org.neo4j.graphalgo.impl.shortestpath.Util.PathCounter;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

public class BetweennessCentralityMulti<ShortestPathCostType> extends BetweennessCentrality<ShortestPathCostType> {

	public int currNodeI = 0;
	
	public BetweennessCentralityMulti(
			SingleSourceShortestPath<ShortestPathCostType> singleSourceShortestPath,
			Set<Node> nodeSet )
	{
		super(singleSourceShortestPath,nodeSet);
	}
	
	@Override
    public void processShortestPaths( Node node,
        SingleSourceShortestPath<ShortestPathCostType> singleSourceShortestPath )
    {
        // Extract predecessors and successors
        Map<Node,List<Relationship>> predecessors = singleSourceShortestPath
            .getPredecessors();
        
        filterMultiEdgePaths(predecessors);
        
        Map<Node,List<Relationship>> successors = Util
            .reversedPredecessors( predecessors );
        PathCounter counter = new Util.PathCounter( predecessors );
        
        getAndUpdateNodeDependency( node, true, successors, counter,
            new HashMap<Node,Double>() );
        
        ++currNodeI;
    }

	protected void filterMultiEdgePaths(Map<Node, List<Relationship>> predecessors) {
		
		for(Entry<Node,List<Relationship>> entry : predecessors.entrySet()){
			Set<Node> visited = new HashSet<Node>();
			Iterator<Relationship> iter = entry.getValue().iterator();
			
			while(iter.hasNext()){
				Node n = iter.next().getOtherNode(entry.getKey());
				if(visited.contains(n)){
					iter.remove();
				} else {
					visited.add(n);
				}
				
			}
		}
	}
}
