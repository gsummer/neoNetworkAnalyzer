package org.networklibrary.neonetworkanalyzer.neo4jalgos.mt;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.tooling.GlobalGraphOperations;
import org.networklibrary.neonetworkanalyzer.neo4jalgos.NetworkUtils;

public class ShortestPathTask implements Callable<Boolean> {

	private Set<Node> starts;
	private GraphDatabaseService graph;

	private Map<Node, NodeBetweenInfo> nodeBetweenness;
	private Map<Node,Double> betweenness;


	public ShortestPathTask(Set<Node> starts, GraphDatabaseService graph){
		this.starts = starts;
		this.graph = graph;

		nodeBetweenness = new HashMap<Node,NodeBetweenInfo>();
		betweenness = new HashMap<Node,Double>();

		try(Transaction tx = graph.beginTx()){
			for(Node node : GlobalGraphOperations.at(graph).getAllNodes()){
				nodeBetweenness.put(node, new NodeBetweenInfo(0, -1, 0.0));
				betweenness.put(node, 0.0);
				tx.success();
			}
		}
	}

	@Override
	public Boolean call() throws Exception {
		for(Node current : starts){
			try(Transaction tx = graph.beginTx()){
				computeNB(current);
				tx.success();
			}

			for (NodeBetweenInfo nodeInfo : nodeBetweenness.values()) {
				nodeInfo.reset();
			}
		}
		return true;
	}
	
	public Map<Node,Double> getBetweenness(){
		return betweenness;
	}


	private void computeNB(Node source){

		LinkedList<Node> done_nodes = new LinkedList<Node>();
		LinkedList<Node> reached = new LinkedList<Node>();

		HashMap<Node, Long> stressDependency = new HashMap<Node, Long>();

		final NodeBetweenInfo sourceNBInfo = nodeBetweenness.get(source);
		sourceNBInfo.setSource();
		reached.add(source);
		stressDependency.put(source, Long.valueOf(0));

		// Use BFS to find shortest paths from source to all nodes in the
		// network
		while (!reached.isEmpty()) {
			final Node current = reached.removeFirst();
			done_nodes.addFirst(current);
			final NodeBetweenInfo currentNBInfo = nodeBetweenness.get(current);
			final Set<Node> neighbors = NetworkUtils.getUniqueNeighbours(current);
			for (Node neighbor : neighbors) {
				final NodeBetweenInfo neighborNBInfo = nodeBetweenness.get(neighbor);
				final List<Relationship> edges = NetworkUtils.getConnectingEdges(current, neighbor);
				final int expectSPLength = currentNBInfo.getSPLength() + 1;
				if (neighborNBInfo.getSPLength() < 0) {
					// Neighbor traversed for the first time
					reached.add(neighbor);
					neighborNBInfo.setSPLength(expectSPLength);
					stressDependency.put(neighbor, Long.valueOf(0));
				}
				// shortest path via current to neighbor found
				if (neighborNBInfo.getSPLength() == expectSPLength) {
					neighborNBInfo.addSPCount(currentNBInfo.getSPCount());
					// check for long overflow 
					if (neighborNBInfo.getSPCount() < 0) {
						//						computeNB = false;
					}
					// add predecessors and outgoing edges, needed for
					// accumulation of betweenness scores
					neighborNBInfo.addPredecessor(current);
					for (final Relationship edge : edges) {
						currentNBInfo.addOutedge(edge);
					}
				}

			}
		}

		// Return nodes in order of non-increasing distance from source
		while (!done_nodes.isEmpty()) {
			final Node current = done_nodes.removeFirst();
			final NodeBetweenInfo currentNBInfo = nodeBetweenness.get(current);
			if (currentNBInfo != null) {
				final long currentStress = stressDependency.get(current).longValue();
				while (!currentNBInfo.isEmptyPredecessors()) {
					final Node predecessor = currentNBInfo.pullPredecessor();
					final NodeBetweenInfo predecessorNBInfo = nodeBetweenness.get(predecessor);
					predecessorNBInfo.addDependency((1.0 + currentNBInfo.getDependency())
							* ((double) predecessorNBInfo.getSPCount() / (double) currentNBInfo
									.getSPCount()));
					// accumulate all sp count
					final long oldStress = stressDependency.get(predecessor).longValue();
					stressDependency.put(predecessor, new Long(oldStress + 1 + currentStress));

				}
				// accumulate node betweenness in each run
				if (!current.equals(source)) {
					//					currentNBInfo.addBetweenness(currentNBInfo.getDependency());
					MultiUtils.addMappedDouble(current, currentNBInfo.getDependency(),betweenness);
				}
			}
		}
	}

	


}
