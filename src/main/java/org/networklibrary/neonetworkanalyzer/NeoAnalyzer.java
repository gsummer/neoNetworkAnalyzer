package org.networklibrary.neonetworkanalyzer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.neo4j.graphalgo.impl.centrality.BetweennessCentrality;
import org.neo4j.graphalgo.impl.centrality.ClosenessCentrality;
import org.neo4j.graphalgo.impl.centrality.CostDivider;
import org.neo4j.graphalgo.impl.centrality.Eccentricity;
import org.neo4j.graphalgo.impl.centrality.ParallellCentralityCalculation;
import org.neo4j.graphalgo.impl.centrality.StressCentrality;
import org.neo4j.graphalgo.impl.shortestpath.SingleSourceShortestPathBFS;
import org.neo4j.graphalgo.impl.util.IntegerAdder;
import org.neo4j.graphalgo.impl.util.IntegerComparator;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;
import org.neo4j.helpers.collection.Iterables;
import org.neo4j.tooling.GlobalGraphOperations;

public class NeoAnalyzer {

	public List<String> analyze(GraphDatabaseService graph) {
		List<String> res = new ArrayList<String>();

		try (Transaction tx = graph.beginTx()){

			RelationshipType[] types = Iterables.toArray(RelationshipType.class,GlobalGraphOperations.at(graph).getAllRelationshipTypes());

//			SingleSourceShortestPath<Double> sssPath = new SingleSourceShortestPathDijkstra<Double>( 0.0, null,
//					new CostEvaluator<Double>()
//					{
//				public Double getCost( Relationship relationship,
//						Direction direction )
//				{
//					return 1.0;
//				}
//					}, new org.neo4j.graphalgo.impl.util.DoubleAdder(),
//					new org.neo4j.graphalgo.impl.util.DoubleComparator(),
//					Direction.BOTH,  types);
			SingleSourceShortestPathBFS sssPath = new SingleSourceShortestPathBFS(null, Direction.BOTH, types);
			Set<Node> allNodes = Iterables.toSet((GlobalGraphOperations.at(graph).getAllNodes()));
			double normFactor = computeNormFactor(allNodes.size());

			ParallellCentralityCalculation<Integer> pcc = new ParallellCentralityCalculation<Integer>(
					sssPath, allNodes );
			
			BetweennessCentrality<Integer> betweennessCentrality = new BetweennessCentrality<Integer>(
					sssPath, allNodes );
			StressCentrality<Integer> stressCentrality = new StressCentrality<Integer>(
					sssPath, allNodes );
			
			ClosenessCentrality<Integer> closenessCentrality = new ClosenessCentrality<Integer>(
					sssPath, new IntegerAdder(), 0, allNodes, new CostDivider<Integer>()
					{
						@Override
						public Integer divideByCost(Double d, Integer c) {
							// TODO Auto-generated method stub
							return d.intValue() / c;
						}

						@Override
						public Integer divideCost(Integer c, Double d) {
							// TODO Auto-generated method stub
							return c / d.intValue();
						}
					} );

			pcc.addCalculation( betweennessCentrality );
			pcc.addCalculation( stressCentrality );
			pcc.addCalculation( closenessCentrality );
			pcc.calculate();
			
			Eccentricity<Integer> eccentricity = new Eccentricity<Integer>( sssPath, 0,allNodes, new IntegerComparator() );

			for(Node node : GlobalGraphOperations.at(graph).getAllNodes()){
				Map<String,Object> stats = new HashMap<String,Object>();
				stats.put("nodeid", node.getId());
				
				double betweenness = betweennessCentrality.getCentrality(node) * normFactor;
					
				stats.put("neo_betweenness", betweenness * 2);
				stats.put("neo_stresscentrality", stressCentrality.getCentrality(node));
				stats.put("neo_closenesscentrality", closenessCentrality.getCentrality(node));
				stats.put("neo_eccentriticy", eccentricity.getCentrality(node));
					
				long edgecount = Iterables.count(node.getRelationships());
				stats.put("neo_edgecount", edgecount);
				stats.put("neo_indegree", Iterables.count(node.getRelationships(Direction.INCOMING)));
				stats.put("neo_outdegree", Iterables.count(node.getRelationships(Direction.OUTGOING)));
				stats.put("neo_issinglenode", (edgecount==0) ? true : false);
				
				//Node Properties:
				//AverageShortestPathLength
				//ClusteringCoefficient
				//NeighborhoodConnectivity
				//PartnerOfMultiEdgedNodePairs
				//SelfLoops
			
				
				// ... and more
				try {
					res.add(toJSON(stats));
				} catch (JsonGenerationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (JsonMappingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		return res;
	}

	String toJSON(Map<String,Object> nodeResults) throws JsonGenerationException, JsonMappingException, IOException{
	
//		StringBuilder res = new StringBuilder();
//		for(Entry<String,Object> entry : nodeResults.entrySet()){
//			res.append(entry.getKey());
//			res.append(" = ");
//			res.append(entry.getValue());
//			res.append("\t");
//		}
		
//		return res.toString();
		
		ObjectMapper m = new ObjectMapper();
		return m.writeValueAsString(nodeResults);
	}

	/* code from: https://github.com/cytoscape/cytoscape-impl/blob/develop/network-analyzer-impl/src/main/java/de/mpg/mpi_inf/bioinf/netanalyzer/UndirNetworkAnalyzer.java
	 */
	protected double computeNormFactor(int count) {
		return (count > 2) ? (1.0 / ((count - 1) * (count - 2))) : 1.0;
	}
}
