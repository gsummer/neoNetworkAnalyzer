package org.networklibrary.neonetworkanalyzer.neo4jalgos;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.neo4j.graphalgo.CostEvaluator;
import org.neo4j.graphalgo.impl.centrality.Eccentricity;
import org.neo4j.graphalgo.impl.centrality.ParallellCentralityCalculation;
import org.neo4j.graphalgo.impl.shortestpath.SingleSourceShortestPath;
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

	protected List<Set<Node>> components = null;

	public List<String> analyze(GraphDatabaseService graph) {
		List<String> res = new ArrayList<String>();

		try(Transaction tx = graph.beginTx()){

			splitComponents(graph);
		}

<<<<<<< HEAD
		System.out.println("num components: " + components.size());
		
=======
		int currComp = 0;
>>>>>>> 85b665c0365b18e3d2772acc82859505fcad5664
		for(Set<Node> component : components){
			System.out.println("starting with component "+currComp+" of size: " + component.size());

			try (Transaction tx = graph.beginTx()){

				RelationshipType[] types = Iterables.toArray(RelationshipType.class,GlobalGraphOperations.at(graph).getAllRelationshipTypes());

				SingleSourceShortestPath sssPath = new SingleSourceShortestPathDijkstra<Integer>(0, null, new CostEvaluator<Integer>(){
					@Override
					public Integer getCost(Relationship relationship, Direction direction) {

						return new Integer(1);
					}
				}, new IntegerAdder(), new IntegerComparator(), Direction.BOTH, types);

				double normFactor = computeNormFactor(component.size());

				BetweennessCentralityMulti<Integer> betweennessCentrality = new BetweennessCentralityMulti<Integer>(sssPath, component );

				StressCentrality<Integer> stressCentrality = new StressCentrality<Integer>(sssPath, component );
				Eccentricity<Integer> eccentricity = new Eccentricity<Integer>( sssPath, 0,component, new IntegerComparator() );
				AverageShortestPath<Integer> avgSP = new AverageShortestPath<Integer>(sssPath, component);

				ParallellCentralityCalculation<Integer> ppc = new ParallellCentralityCalculation<Integer>(sssPath, component);
				ppc.addCalculation(stressCentrality);
				ppc.addCalculation(eccentricity);
				ppc.addCalculation(betweennessCentrality);
				ppc.addCalculation(avgSP);
				ppc.calculate();

				int maxEccentricity = findMaxEccentricity(eccentricity,component);

				ClusteringCoeff clustCoeff = new ClusteringCoeff();
				NeighbourhoodConnectivity neighbourhoodConn = new NeighbourhoodConnectivity();
				MultiEdgePairs multiEdgePairs = new MultiEdgePairs();
				TopologicalCoeff topoCoeff = new TopologicalCoeff();
				Radiality<Integer> radiality = new Radiality<>(maxEccentricity, avgSP);

				for(Node node : component){
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
			System.out.println("finished with component " + currComp);
			++currComp;
			
		}

		return res;
		}


		protected void splitComponents(GraphDatabaseService graph) {
			components = new ArrayList<Set<Node>>();
			Set<Node> visited = new HashSet<Node>();

			for(Node n : GlobalGraphOperations.at(graph).getAllNodes()){
				if(visited.contains(n))
					continue;

				Set<Node> currComponent = new HashSet<Node>();

				visited.add(n);
				currComponent.add(n);

				Queue<Node> q = new LinkedList<Node>();

				q.addAll(NetworkUtils.getUniqueNeighbours(n));

				while(!q.isEmpty()){
					Node curr = q.poll();
					if(visited.contains(curr))
						continue;

					visited.add(curr);
					currComponent.add(curr);
					q.addAll(NetworkUtils.getUniqueNeighbours(curr));
				}

				components.add(currComponent);
			}

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
