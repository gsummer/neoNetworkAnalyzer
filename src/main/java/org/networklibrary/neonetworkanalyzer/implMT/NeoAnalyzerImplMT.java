package org.networklibrary.neonetworkanalyzer.implMT;

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
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.helpers.collection.Iterables;
import org.neo4j.tooling.GlobalGraphOperations;
import org.networklibrary.neonetworkanalyzer.NeoAnalyzer;
import org.networklibrary.neonetworkanalyzer.statistics.ClusteringCoeff;
import org.networklibrary.neonetworkanalyzer.statistics.MultiEdgePairs;
import org.networklibrary.neonetworkanalyzer.statistics.NeighbourhoodConnectivity;
import org.networklibrary.neonetworkanalyzer.statistics.NetworkUtils;
import org.networklibrary.neonetworkanalyzer.statistics.RadialityMT;
import org.networklibrary.neonetworkanalyzer.statistics.TopologicalCoeff;

public class NeoAnalyzerImplMT implements NeoAnalyzer {

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
	private int chunksPerThread = 1;
	private ExecutorService execService = null;

	public NeoAnalyzerImplMT(boolean eccentricityFlag, boolean betweennessFlag,
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

		threadCount = Math.min(16, Math.max(1, Runtime.getRuntime().availableProcessors() - 1));
		execService = Executors.newFixedThreadPool(threadCount);

		System.out.println("num threads: " + threadCount);
	}

	public NeoAnalyzerImplMT(boolean eccentricityFlag, boolean betweennessFlag,
			boolean stressFlag, boolean avgSPFlag, boolean radialityFlag,
			boolean topoCoeffFlag, boolean neighbourhoodConnFlag,
			boolean multiEdgePairsFlag, boolean closenessFlag,
			boolean clustCoeffFlag,int numThreads) {
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

		threadCount = numThreads;
		execService = Executors.newFixedThreadPool(threadCount);

		System.out.println("num threads: " + threadCount);
	}


	public List<String> analyze(GraphDatabaseService graph,boolean saveInGraph) {
		List<String> res = new ArrayList<String>();

		Map<Node,Double> betweenness = null;
		Map<Node,Long> stress = null;
		Map<Node,Double> avgSP = null;
		Map<Node,Long> eccentricity = null;

		if(doBetweenness()){
			betweenness  = new HashMap<Node,Double>();
		}

		if(doStress()){
			stress = new HashMap<Node,Long>();
		}

		if(doAvgSP()){
			avgSP = new HashMap<Node,Double>();
		}

		if(doEccentritity()){
			eccentricity = new HashMap<Node,Long>();
		}

		try(Transaction tx = graph.beginTx()){

			for(Node n : GlobalGraphOperations.at(graph).getAllNodes()){
				betweenness.put(n, new Double(0.0));
				stress.put(n, new Long(0));
				avgSP.put(n, new Double(0.0));
				eccentricity.put(n, new Long(0));
			}

			splitComponents(graph);
			tx.success();
		}

		System.out.println("num components: " + components.size());
		int currComp = 0;

		for(Set<Node> component : components){

			System.out.println("starting with component "+ currComp +" of size: " + component.size());

			double normFactor = computeNormFactor(component.size());

			if(doBetweenness() || doStress() || doAvgSP() || doEccentritity()){

				List<ParallelCentralityTask> tasks = new ArrayList<ParallelCentralityTask>();

				if(component.size() > 10){

					int numChunks = threadCount * chunksPerThread;
					int chunkSize = component.size() / numChunks;

					Set<Node> chunk = new HashSet<Node>();

					System.out.println("chunkSize = " + chunkSize);

					// make a nicer chunking
					int i = chunkSize;
					for(Node n : component){

						if(i == 0){
							System.out.println("size of chunk = " + chunk.size());
							tasks.add(prepChunk(chunk,component, graph));
							chunk = new HashSet<Node>();
							i = chunkSize;
						}
						chunk.add(n);
						--i;
					}
					// submit the leftovers
					System.out.println("size of chunk = " + chunk.size());
					tasks.add(prepChunk(chunk,component, graph));
				} else {
					ParallelCentralityTask spt = prepChunk(component,component, graph);
					tasks.add(spt);
				}

				try {
					execService.invokeAll(tasks);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				for(Node n : component){

					long sum = 0L;
					long num = 0L;

					for(ParallelCentralityTask task : tasks){

						if(doBetweenness())
							MultiUtils.addMapped(n, task.getBetweennessCentrality().getCentrality(n), betweenness);

						if(doStress())
							MultiUtils.addMapped(n, task.getStressCentrality().getCentrality(n), stress);

						if(doEccentritity()){
							Integer ecc = task.getEccentricity().getCentrality(n);

							if(ecc != null && ecc > eccentricity.get(n)){
								eccentricity.put(n, task.getEccentricity().getCentrality(n).longValue());
							}
						}

						if(doAvgSP()){
							sum += task.getAvgSP().getSumPaths(n);
							num += task.getAvgSP().getNumPaths(n);
						}
					}

					if(doAvgSP() && num > 0)
						avgSP.put(n, sum / (double)num);

				}

			}

			// combine results
			TopologicalCoeff topoCoeff = null;
			RadialityMT<Integer> radiality = null;
			ClusteringCoeff clustCoeff = null;
			NeighbourhoodConnectivity neighbourhoodConn = null;
			MultiEdgePairs multiEdgePairs = null;

			if(doTopoCoeff()){
				topoCoeff = new TopologicalCoeff();
			}

			if(doClusteringCoeff()){
				clustCoeff = new ClusteringCoeff();
			}

			if(doNeighbourhoodConnectivity()){
				neighbourhoodConn = new NeighbourhoodConnectivity();
			}

			if(doMultiEdgePairs()){
				multiEdgePairs = new MultiEdgePairs();
			}

			if(doRadiality()){
				long maxEccentricity = findMaxEccentricity(eccentricity,component);
				radiality = new RadialityMT<Integer>(maxEccentricity, avgSP);
			}

			for(Node node : component){
				try (Transaction tx = graph.beginTx()){
					Map<String, Object> stats = new HashMap<String,Object>();

					Iterable<Relationship> rels  = node.getRelationships();
					long edgecount = Iterables.count(rels);

					stats.put("neo_edgecount", edgecount);
					stats.put("neo_indegree", Iterables.count(node.getRelationships(Direction.INCOMING)));
					stats.put("neo_outdegree", Iterables.count(node.getRelationships(Direction.OUTGOING)));
					stats.put("neo_issinglenode", (edgecount==0) ? true : false);

					if(edgecount == 0){

						stats.put("neo_indegree", 0);
						stats.put("neo_outdegree", 0);
						if(doBetweenness())
							stats.put("neo_betweenness", 0.0);

						if(doStress())
							stats.put("neo_stresscentrality", 0L);

						if(doCloseness())
							stats.put("neo_closenesscentrality", 0.0);

						if(doEccentritity())
							stats.put("neo_eccentriticy", 0);

						if(doAvgSP())
							stats.put("neo_avgSP", 0.0);

						if(doClusteringCoeff())
							stats.put("neo_clustcoeff", 0.0);

						if(doNeighbourhoodConnectivity())
							stats.put("neo_neighbourhoodconnectivity",0.0);

						if(doMultiEdgePairs())
							stats.put("neo_multiedgepairs",0L);

						if(doTopoCoeff())
							stats.put("neo_topologicalcoeff", 0.0);

						if(doRadiality())
							stats.put("neo_radiality", 0.0);

					} else {
						Set<Node> thisNode = new HashSet<Node>();
						thisNode.add(node);

						stats.put("neo_indegree", Iterables.count(node.getRelationships(Direction.INCOMING)));
						stats.put("neo_outdegree", Iterables.count(node.getRelationships(Direction.OUTGOING)));

						if(doBetweenness())
							stats.put("neo_betweenness", betweenness.get(node) * normFactor * 2);

						if(doStress())
							stats.put("neo_stresscentrality", stress.get(node));

						if(doCloseness())
							stats.put("neo_closenesscentrality", (avgSP.get(node)> 0) ? (1/avgSP.get(node)) : 0.0);

						if(doEccentritity())
							stats.put("neo_eccentriticy", eccentricity.get(node));

						if(doAvgSP()){
							stats.put("neo_avgSP", avgSP.get(node));
						}

						if(doClusteringCoeff())
							stats.put("neo_clustcoeff", clustCoeff.calcClusteringCoeff(node));

						if(doNeighbourhoodConnectivity())
							stats.put("neo_neighbourhoodconnectivity",neighbourhoodConn.calcNeighbourhoodConnectivity(node));

						if(doMultiEdgePairs())
							stats.put("neo_multiedgepairs",multiEdgePairs.calcMultipleEdgePairs(node));

						if(doTopoCoeff())
							stats.put("neo_topologicalcoeff", topoCoeff.calcTopologicalCoeff(node));

						if(doRadiality())
							stats.put("neo_radiality", radiality.calcRadiality(node));
					}

					try {
						if(saveInGraph){
							addToGraph(node,stats,graph);
						}
						stats.put("nodeid", node.getId());
						res.add(toJSON(stats));

						// how about actual error handling? :)
					} catch (JsonGenerationException e) {
						e.printStackTrace();
					} catch (JsonMappingException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
					tx.success();
				}
			}
//			System.out.println("finished with component " + currComp);
			++currComp;
		}

		// thread cleanup: all tasks are done because the invokeAll blocks 
		try {
			execService.shutdown();
			execService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
		} catch (InterruptedException e) {
			System.out.println("stopping the execService failed");
			e.printStackTrace();
			return null;
		}

		return res;
	}


	private boolean doClusteringCoeff() {
		return clustCoeffFlag;
	}


	private boolean doCloseness() {
		return closenessFlag;
	}


	private boolean doMultiEdgePairs() {
		return multiEdgePairsFlag;
	}


	private boolean doNeighbourhoodConnectivity() {
		return neighbourhoodConnFlag;
	}

	private boolean doTopoCoeff() {
		return topoCoeffFlag;
	}

	private boolean doRadiality() {
		return radialityFlag;
	}

	private boolean doAvgSP() {
		return avgSPFlag;
	}

	private boolean doStress() {
		return stressFlag;
	}

	private boolean doBetweenness() {
		return betweennessFlag;
	}

	private boolean doEccentritity() {
		return eccentricityFlag;
	}

	protected ParallelCentralityTask prepChunk(Set<Node> chunk, Set<Node> component,GraphDatabaseService graph){
		ParallelCentralityTask pct = new ParallelCentralityTask(chunk,component, graph,doRadiality() || doEccentritity(), doBetweenness(), doStress(),doRadiality() || doCloseness() || doAvgSP());
		return pct;
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

	protected long findMaxEccentricity(Map<Node,Long> eccentricity,Set<Node> nodes) {
		long max = 0;
		for(Node n : nodes){
			long eccentriticy  = eccentricity.get(n);
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
