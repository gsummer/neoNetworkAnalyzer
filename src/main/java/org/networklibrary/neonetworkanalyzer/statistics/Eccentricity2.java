package org.networklibrary.neonetworkanalyzer.statistics;

import java.util.Comparator;
import java.util.Set;

import org.neo4j.graphalgo.impl.centrality.Eccentricity;
import org.neo4j.graphalgo.impl.shortestpath.SingleSourceShortestPath;
import org.neo4j.graphdb.Node;

public class Eccentricity2<ShortestPathCostType> extends Eccentricity<ShortestPathCostType> {

	public Eccentricity2(
			SingleSourceShortestPath<ShortestPathCostType> singleSourceShortestPath,
			ShortestPathCostType zeroValue, Set<Node> nodeSet,
			Comparator<ShortestPathCostType> distanceComparator) {
		super(singleSourceShortestPath, zeroValue, nodeSet, distanceComparator);
	}
	
	@Override
    public ShortestPathCostType getCentrality( Node node )
    {
        ShortestPathCostType centrality = centralities.get( node );
        if ( centrality == null )
        {
            return null;
        }
        // Not calculated yet, or if it actually is 0 it is very fast to
        // compute so just do it.
        if ( centrality.equals( zeroValue ) && !doneCalculation )
        {
            singleSourceShortestPath.reset();
            singleSourceShortestPath.setStartNode( node );
            processShortestPaths( node, singleSourceShortestPath );
        }
        // When the value is calculated, just retrieve it normally
        return centralities.get( node );
    }

}
