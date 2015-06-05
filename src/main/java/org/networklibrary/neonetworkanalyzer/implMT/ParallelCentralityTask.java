package org.networklibrary.neonetworkanalyzer.implMT;

import java.util.Set;
import java.util.concurrent.Callable;

import org.neo4j.graphalgo.CostEvaluator;
import org.neo4j.graphalgo.impl.centrality.BetweennessCentrality;
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
import org.networklibrary.neonetworkanalyzer.statistics.AverageShortestPathMulti;
import org.networklibrary.neonetworkanalyzer.statistics.BetweennessCentralityMulti;
import org.networklibrary.neonetworkanalyzer.statistics.Eccentricity2;
import org.networklibrary.neonetworkanalyzer.statistics.StressCentrality;

public class ParallelCentralityTask implements Callable<Boolean> {

	protected Set<Node> starts;
	protected GraphDatabaseService graph;
	
	protected ParallellCentralityCalculation<Integer> ppc;

	protected BetweennessCentrality<Integer> betweennessCentrality = null;
	protected StressCentrality<Integer> stressCentrality = null;
	protected Eccentricity2<Integer> eccentricity = null;
	protected AverageShortestPathMulti<Integer> avgSP = null;

	public ParallelCentralityTask(Set<Node> chunk,Set<Node> component, GraphDatabaseService graph, boolean eccentricityFlag, boolean betweennessFlag, boolean stressFlag, boolean avgSPFlag) {
		this.starts = chunk;
		this.graph = graph;
		

		RelationshipType[] types = null;
		try (Transaction tx = graph.beginTx()){

			types = Iterables.toArray(RelationshipType.class,GlobalGraphOperations.at(graph).getAllRelationshipTypes());
			tx.success();
		}

		SingleSourceShortestPath<Integer> sssPath = new SingleSourceShortestPathDijkstra<Integer>(0, null, new CostEvaluator<Integer>(){
			@Override
			public Integer getCost(Relationship relationship, Direction direction) {

				return new Integer(1);
			}
		}, new IntegerAdder(), new IntegerComparator(), Direction.BOTH, types);

		ppc = new LogParallelCentralityCalculation<Integer>(sssPath, chunk);

		if(eccentricityFlag){
			eccentricity = new Eccentricity2<Integer>( sssPath, 0,component, new IntegerComparator() );
			ppc.addCalculation(eccentricity);
		}

		if(betweennessFlag){
			betweennessCentrality = new BetweennessCentralityMulti<Integer>(sssPath, component );
			ppc.addCalculation(betweennessCentrality);
		}

		if(stressFlag){
			stressCentrality = new StressCentrality<Integer>(sssPath, component );
			ppc.addCalculation(stressCentrality);
		}

		if(avgSPFlag){
			avgSP = new AverageShortestPathMulti<Integer>(sssPath, component);
			ppc.addCalculation(avgSP);
		}

	}

	@Override
	public Boolean call() throws Exception {

		try (Transaction tx = graph.beginTx()){
			ppc.calculate();
			tx.success();
		}
		return true;
	}

	public BetweennessCentrality<Integer> getBetweennessCentrality() {
		return betweennessCentrality;
	}

	public StressCentrality<Integer> getStressCentrality() {
		return stressCentrality;
	}

	public Eccentricity2<Integer> getEccentricity() {
		return eccentricity;
	}

	public AverageShortestPathMulti<Integer> getAvgSP() {
		return avgSP;
	}
}
