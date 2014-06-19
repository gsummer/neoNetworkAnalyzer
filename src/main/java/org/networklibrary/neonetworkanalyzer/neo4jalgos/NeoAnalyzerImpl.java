package org.networklibrary.neonetworkanalyzer.neo4jalgos;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.neo4j.cypher.internal.compiler.v2_1.functions.ToLower;
import org.neo4j.graphalgo.CostEvaluator;
import org.neo4j.graphalgo.impl.centrality.BetweennessCentrality;
import org.neo4j.graphalgo.impl.centrality.Eccentricity;
import org.neo4j.graphalgo.impl.centrality.ParallellCentralityCalculation;
import org.neo4j.graphalgo.impl.shortestpath.SingleSourceShortestPath;
import org.neo4j.graphalgo.impl.shortestpath.SingleSourceShortestPathBFS;
import org.neo4j.graphalgo.impl.shortestpath.SingleSourceShortestPathDijkstra;
import org.neo4j.graphalgo.impl.util.IntegerAdder;
import org.neo4j.graphalgo.impl.util.IntegerComparator;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;
import org.neo4j.helpers.collection.Iterables;
import org.neo4j.tooling.GlobalGraphOperations;
import org.networklibrary.neonetworkanalyzer.NeoAnalyzer;

public class NeoAnalyzerImpl implements NeoAnalyzer {

	public List<String> analyze(GraphDatabaseService graph) {
		List<String> res = new ArrayList<String>();

		try (Transaction tx = graph.beginTx()){
			System.out.println("num nodes:" + Iterables.count(GlobalGraphOperations.at(graph).getAllNodes()));
			System.out.println("num nodes:" + Iterables.count(GlobalGraphOperations.at(graph).getAllRelationships()));

			RelationshipType[] types = Iterables.toArray(RelationshipType.class,GlobalGraphOperations.at(graph).getAllRelationshipTypes());

//			SingleSourceShortestPath sssPath = new SingleSourceShortestPathBFS(null, Direction.BOTH, types);
			
			SingleSourceShortestPath sssPath = new SingleSourceShortestPathDijkstra<Integer>(0, null, new CostEvaluator<Integer>(){
				@Override
				public Integer getCost(Relationship relationship, Direction direction) {
					
					return new Integer(1);
				}
			}, new IntegerAdder(), new IntegerComparator(), Direction.BOTH, types);
//			
			Set<Node> allNodes = new HashSet<Node>();
			
			// this is to check isolated nodes and not calculate certain parameters for it
			// because they null point: Eccentricity and Closeness
			for(Node n : (GlobalGraphOperations.at(graph).getAllNodes())){
//				if(n.getDegree() > 0)
					allNodes.add(n);	
			}
			
			double normFactor = computeNormFactor(allNodes.size());

			BetweennessCentralityMulti<Integer> betweennessCentrality = new BetweennessCentralityMulti<Integer>(sssPath, allNodes );
			
			StressCentrality<Integer> stressCentrality = new StressCentrality<Integer>(sssPath, allNodes );
			Eccentricity<Integer> eccentricity = new Eccentricity<Integer>( sssPath, 0,allNodes, new IntegerComparator() );
			AverageShortestPath<Integer> avgSP = new AverageShortestPath<Integer>(sssPath, allNodes);

			ParallellCentralityCalculation<Integer> ppc = new ParallellCentralityCalculation<Integer>(sssPath, allNodes);
			ppc.addCalculation(stressCentrality);
			ppc.addCalculation(eccentricity);
			ppc.addCalculation(betweennessCentrality);
			ppc.addCalculation(avgSP);
			ppc.calculate();
			
			int maxEccentricity = findMaxEccentricity(eccentricity,allNodes);
			
			ClusteringCoeff clustCoeff = new ClusteringCoeff();
			NeighbourhoodConnectivity neighbourhoodConn = new NeighbourhoodConnectivity();
			MultiEdgePairs multiEdgePairs = new MultiEdgePairs();
			TopologicalCoeff topoCoeff = new TopologicalCoeff();
			Radiality<Integer> radiality = new Radiality<>(maxEccentricity, avgSP);
			
			for(Node node : GlobalGraphOperations.at(graph).getAllNodes()){
				Map<String, Object> stats = new HashMap<String,Object>();
				stats.put("nodeid", node.getId());

				Iterable<Relationship> rels  = node.getRelationships();
				long edgecount = Iterables.count(rels);

				stats.put("neo_edgecount", edgecount);
				stats.put("neo_indegree", Iterables.count(node.getRelationships(Direction.INCOMING)));
				stats.put("neo_outdegree", Iterables.count(node.getRelationships(Direction.OUTGOING)));
				stats.put("neo_issinglenode", (edgecount==0) ? true : false);

				if(edgecount == 0){

				} else {
					Set<Node> thisNode = new HashSet<Node>();
					thisNode.add(node);

					double betweenness = betweennessCentrality.getCentrality(node) * normFactor;
					double closeness = (avgSP.getCentrality(node) > 0) ? (1/avgSP.getCentrality(node)) : 0.0;

					stats.put("neo_name", node.getProperty("name","unknown"));
					stats.put("neo_betweenness", betweenness * 2);
					stats.put("neo_stresscentrality", stressCentrality.getCentrality(node));
					stats.put("neo_closenesscentrality", closeness);
					stats.put("neo_eccentriticy", eccentricity.getCentrality(node));
					stats.put("neo_avgSP", avgSP.getCentrality(node));
					stats.put("neo_clustcoeff", clustCoeff.calcClusteringCoeff(node));
					stats.put("neo_neighbourhoodconnectivity",neighbourhoodConn.calcNeighbourhoodConnectivity(node));
					stats.put("neo_multiedgepairs",multiEdgePairs.calcMultipleEdgePairs(node));
					stats.put("neo_topologicalcoeff", topoCoeff.calcTopologicalCoeff(node));
					stats.put("neo_radiality", radiality.calcRadiality(node));
				}

				try {
					res.add(toJSON(stats));
				} catch (JsonGenerationException e) {
					e.printStackTrace();
				} catch (JsonMappingException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return res;
	}

	
	protected int findMaxEccentricity(Eccentricity<Integer> eccentricity,
			Set<Node> allNodes) {
		int max = 0;
		
		for(Node n : allNodes){
			if(eccentricity.getCentrality(n) > max)
				max = eccentricity.getCentrality(n);
		}
		
		return max;
	}
	
	String toJSON(Map<String,Object> nodeResults) throws JsonGenerationException, JsonMappingException, IOException{
		ObjectMapper m = new ObjectMapper();
		return m.writeValueAsString(nodeResults);
	}
	
	/* code from: https://github.com/cytoscape/cytoscape-impl/blob/develop/network-analyzer-impl/src/main/java/de/mpg/mpi_inf/bioinf/netanalyzer/UndirNetworkAnalyzer.java
	 */
	protected double computeNormFactor(int count) {
		return (count > 2) ? (1.0 / ((count - 1) * (count - 2))) : 1.0;
	}

	
}
