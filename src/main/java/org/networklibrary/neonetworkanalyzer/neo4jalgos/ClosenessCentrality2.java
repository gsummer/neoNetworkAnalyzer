package org.networklibrary.neonetworkanalyzer.neo4jalgos;

import java.util.Set;

import org.neo4j.graphalgo.CostAccumulator;
import org.neo4j.graphalgo.impl.centrality.ShortestPathBasedCentrality;
import org.neo4j.graphalgo.impl.shortestpath.SingleSourceShortestPath;
import org.neo4j.graphalgo.impl.util.DoubleAdder;
import org.neo4j.graphdb.Node;

public class ClosenessCentrality2<ShortestPathCostType> extends
		ShortestPathBasedCentrality<Double, ShortestPathCostType> {

	CostDivider2<ShortestPathCostType> centralityDivider;
	CostAccumulator<ShortestPathCostType> pathCostAccumulator;
	
	
	public ClosenessCentrality2(
	        SingleSourceShortestPath<ShortestPathCostType> singleSourceShortestPath,
	        CostAccumulator<ShortestPathCostType> pathCostAccumulator,
	        Double zeroValue, Set<Node> nodeSet,
	        CostDivider2<ShortestPathCostType> centralityDivider )
	    {
	        super( singleSourceShortestPath, new DoubleAdder(), zeroValue,
	            nodeSet );
	        this.centralityDivider = centralityDivider;
	        this.pathCostAccumulator = pathCostAccumulator;
	    }
		
	@Override
    public Double getCentrality( Node node )
    {
        Double centrality = centralities.get( node );
        if ( centrality == null )
        {
            return null;
        }
        // Not calculated yet, or if it actually is 0 it is very fast to
        // compute so just do it.
        if ( centrality.equals( zeroValue ) )
        {
            singleSourceShortestPath.reset();
            singleSourceShortestPath.setStartNode( node );
            processShortestPaths( node, singleSourceShortestPath );
        }
        // When the value is calculated, just retrieve it normally
        return centralities.get( node );
    }

    @Override
    public void processShortestPaths( Node node,
        SingleSourceShortestPath<ShortestPathCostType> singleSourceShortestPath )
    {
        ShortestPathCostType shortestPathSum = null;
        for ( Node targetNode : nodeSet )
        {
            if ( shortestPathSum == null )
            {
                shortestPathSum = singleSourceShortestPath.getCost( targetNode );
            }
            else
            {
                shortestPathSum = pathCostAccumulator.addCosts(
                    shortestPathSum, singleSourceShortestPath
                        .getCost( targetNode ) );
            }
        }
        // TODO: what should the result be when sum is 0 ?
        if ( !shortestPathSum.equals( zeroValue ) )
        {
            setCentralityForNode( node, centralityDivider.divideByCost( 1.0,
                shortestPathSum ) );
        }
    }

}
