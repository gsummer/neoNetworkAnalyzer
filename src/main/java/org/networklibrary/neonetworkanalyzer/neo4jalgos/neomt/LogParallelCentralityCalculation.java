package org.networklibrary.neonetworkanalyzer.neo4jalgos.neomt;

import java.util.Set;

import org.neo4j.graphalgo.impl.centrality.ParallellCentralityCalculation;
import org.neo4j.graphalgo.impl.centrality.ShortestPathBasedCentrality;
import org.neo4j.graphalgo.impl.shortestpath.SingleSourceShortestPath;
import org.neo4j.graphdb.Node;

public class LogParallelCentralityCalculation<ShortestPathCostType> extends
		ParallellCentralityCalculation<ShortestPathCostType> {

	public LogParallelCentralityCalculation(
	        SingleSourceShortestPath<ShortestPathCostType> singleSourceShortestPath,
	        Set<Node> nodeSet )
	    {
	        super(singleSourceShortestPath,nodeSet);
	    }

	    /**
	     * This adds a centrality measure to be included in the calculation.
	     * @param shortestPathBasedCentrality
	     *            The centrality algorithm.
	     */
	    public void addCalculation(
	        ShortestPathBasedCentrality<?,ShortestPathCostType> shortestPathBasedCentrality )
	    {
	        if ( doneCalculation )
	        {
	            throw new RuntimeException(
	                "Trying to add a centrality calculation to a parallell computation that has already been done." );
	        }
	        calculations.add( shortestPathBasedCentrality );
	        shortestPathBasedCentrality.skipCalculation();
	    }

	    /**
	     * Method that will perform the calculation. After this we are of course
	     * unable to add more measures to this object.
	     */
	    public void calculate()
	    {
	        // Don't do it more than once
	        if ( doneCalculation )
	        {
	            return;
	        }
	        doneCalculation = true;
	        // For all nodes...
	        int i = 0;
	        for ( Node startNode : nodeSet )
	        {
	        	long start = System.currentTimeMillis();
	            // Prepare the singleSourceShortestPath
	            singleSourceShortestPath.reset();
	            singleSourceShortestPath.setStartNode( startNode );
	            // Process
	            for ( ShortestPathBasedCentrality<?,ShortestPathCostType> calculation : calculations )
	            {
	                calculation.processShortestPaths( startNode,
	                    singleSourceShortestPath );
	            }
	            long end = System.currentTimeMillis();
	            ++i;
	            System.out.println(Thread.currentThread().getId() + ": node " + i + ": per node time: " + (end - start));
	        }
	    }
}
