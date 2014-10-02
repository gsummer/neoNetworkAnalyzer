package org.networklibrary.neonetworkanalyzer.neo4jalgos;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.neo4j.graphalgo.impl.centrality.Eccentricity;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.tooling.GlobalGraphOperations;
import org.networklibrary.neonetworkanalyzer.NeoAnalyzer;
import org.networklibrary.neonetworkanalyzer.neo4jalgos.mt.MultiUtils;
import org.networklibrary.neonetworkanalyzer.neo4jalgos.mt.ShortestPathTask;

public class NeoAnalyzerMultiImpl implements NeoAnalyzer {

	protected List<Set<Node>> components = null;
	private boolean eccentricityFlag;
	private boolean betweennessFlag;
	private boolean stressFlag;
	private boolean avgSPFlag;
	private boolean radialityFlag;
	private boolean topoCoeffFlag;
	private boolean neighbourhoodConnFlag;
	private boolean multiEdgePairsFlag;
	private boolean closenessFlag;
	private boolean clustCoeffFlag;

	private int threadCount = 2;
	private ExecutorService execService = null;

	public NeoAnalyzerMultiImpl(boolean eccentricityFlag, boolean betweennessFlag,
			boolean stressFlag, boolean avgSPFlag, boolean radialityFlag,
			boolean topoCoeffFlag, boolean neighbourhoodConnFlag,
			boolean multiEdgePairsFlag, boolean closenessFlag,
			boolean clustCoeffFlag) {
		super();
		this.eccentricityFlag = eccentricityFlag;
		this.betweennessFlag = betweennessFlag;
		this.stressFlag = stressFlag;
		this.avgSPFlag = avgSPFlag;
		this.radialityFlag = radialityFlag;
		this.topoCoeffFlag = topoCoeffFlag;
		this.neighbourhoodConnFlag = neighbourhoodConnFlag;
		this.multiEdgePairsFlag = multiEdgePairsFlag;
		this.closenessFlag = closenessFlag;
		this.clustCoeffFlag = clustCoeffFlag;

		// decide numThreads
		// setup exec
		threadCount = Math.min(4, Math.max(1, Runtime.getRuntime().availableProcessors() - 1));
		execService = Executors.newFixedThreadPool(threadCount);
//				execService = Executors.newSingleThreadExecutor();
		System.out.println("num threads: " + threadCount);

	}

	public List<String> analyze(GraphDatabaseService graph,boolean saveInGraph) {
		List<String> res = new ArrayList<String>();

		Map<Node,Double> betweenness = new HashMap<Node,Double>();

		try(Transaction tx = graph.beginTx()){

			for(Node n : GlobalGraphOperations.at(graph).getAllNodes()){
				betweenness.put(n, 0.0);
			}

			splitComponents(graph);
			tx.success();
		}

		System.out.println("num components: " + components.size());
		int currComp = 0;

		for(Set<Node> component : components){
			System.out.println("starting with component "+ currComp +" of size: " + component.size());
			double normFactor = computeNormFactor(component.size());

			if(component.size() > 10){

				List<ShortestPathTask> spts = new ArrayList<ShortestPathTask>();

				int numChunks = threadCount;
				int chunkSize = component.size() / numChunks;

				Set<Node> chunk = new HashSet<Node>();

				System.out.println("chunkSize = " + chunkSize);

				int i = chunkSize;
				for(Node n : component){

					if(i == 0){
						System.out.println("size of chunk = " + chunk.size());
						spts.add(prepChunk(chunk, graph));
						chunk = new HashSet<Node>();
						i = chunkSize;
					}
					chunk.add(n);
					--i;
				}
				// submit the leftovers
				System.out.println("size of chunk = " + chunk.size());
				spts.add(prepChunk(chunk, graph));


				try {
					execService.invokeAll(spts);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				// merge the results
				for(ShortestPathTask spt : spts){
					MultiUtils.mergeIntoMap(betweenness, spt.getBetweenness());
				}

			} else {
				ShortestPathTask spt = new ShortestPathTask(component, graph);
				try {
					spt.call();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			for(Node n : component){
				res.add(n.getId() + "\t" +betweenness.get(n) * normFactor);
			}


			System.out.println("finished with component " + currComp);
			++currComp;
		}

		try {
			execService.shutdown();
			execService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
		} catch (InterruptedException e) {

			e.printStackTrace();
		}

		return res;
	}


	protected ShortestPathTask prepChunk(Set<Node> chunk,GraphDatabaseService graph){
		ShortestPathTask spt = new ShortestPathTask(chunk, graph);
		return spt;
	}


	private boolean doClusteringCoeff() {
		return clustCoeffFlag;
	}


	private boolean doCloseness() {
		return closenessFlag;
	}


	private boolean doMultiEdgePairs() {
		// TODO Auto-generated method stub
		return multiEdgePairsFlag;
	}


	private boolean doNeighbourhoodConnectivity() {
		// TODO Auto-generated method stub
		return neighbourhoodConnFlag;
	}


	private boolean doTopoCoeff() {
		// TODO Auto-generated method stub
		return topoCoeffFlag;
	}


	private boolean doRadiality() {
		// TODO Auto-generated method stub
		return radialityFlag;
	}


	private boolean doAvgSP() {
		// TODO Auto-generated method stub
		return avgSPFlag;
	}


	private boolean doStress() {
		// TODO Auto-generated method stub
		return stressFlag;
	}


	private boolean doBetweenness() {
		// TODO Auto-generated method stub
		return betweennessFlag;
	}


	private boolean doEccentritity() {
		// TODO Auto-generated method stub
		return eccentricityFlag;
	}


	private void addToGraph(Node n, Map<String, Object> stats,GraphDatabaseService graph) {

		try(Transaction tx = graph.beginTx()){
			for(Entry<String,Object> e : stats.entrySet()){
				n.setProperty(e.getKey(), e.getValue());
			}

			tx.success();
		}

	}


	protected void splitComponents(GraphDatabaseService graph) {
		components = new ArrayList<Set<Node>>();
		Set<Node> visited = new HashSet<Node>();

		int currStep = 0;

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
				++currStep;

				if(currStep % 1000 == 0){
					System.out.println("current step = "+ currStep);
				}

			}

			components.add(currComponent);
		}

	}

	protected int findMaxEccentricity(Eccentricity<Integer> eccentricity,Set<Node> nodes) {
		int max = 0;
		for(Node n : nodes){
			int eccentriticy  = eccentricity.getCentrality(n);
			if(eccentriticy > max)
				max = eccentriticy;
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
