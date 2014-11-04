package org.networklibrary.neonetworkanalyzer;

import java.util.ArrayList;
import java.util.List;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.networklibrary.neonetworkanalyzer.neo4jalgos.NeoAnalyzerImpl;

/**
 * Hardcoded test space!!
 *
 */
public class App 
{
	public static void main( String[] args )
	{
		if(args.length != 3){
			System.out.println("not enough arguments; need graph and numRuns");
			return;
		}

		String graphloc = args[0];
		int numRuns = Integer.parseInt(args[1]);
		int numThreads = Integer.parseInt(args[2]);

		numThreads = 1;

		List<Long> durations = new ArrayList<Long>();

//		GraphDatabaseService g = new GraphDatabaseFactory().newEmbeddedDatabase(graphloc);
		GraphDatabaseService g = new GraphDatabaseFactory().newEmbeddedDatabaseBuilder(graphloc)
//				.setConfig(GraphDatabaseSettings.cache_type, "strong")
				.newGraphDatabase();

//		try(Transaction tx = g.beginTx()){
//		for(Node n : GlobalGraphOperations.at(g).getAllNodes()){
//			
//		}
//		for(Relationship r : GlobalGraphOperations.at(g).getAllRelationships()){
//			
//		}
//		}
		
		for(int i = 0; i < numRuns; ++i){


				NeoAnalyzerImpl analyzer = new NeoAnalyzerImpl(true,false,true,false,false,false,false,false,false,false);
//			NeoAnalyzerMultiImpl analyzer = new NeoAnalyzerMultiImpl(true,true,true,true,true,true,true,true,true,true);
			
//			boolean eccentricityFlag
//			boolean betweennessFlag,
//			boolean stressFlag, 
//			boolean avgSPFlag, 
//			boolean radialityFlag,
//			boolean topoCoeffFlag, 
//			boolean neighbourhoodConnFlag,
//			boolean multiEdgePairsFlag, 
//			boolean closenessFlag,
//			boolean clustCoeffFlag
			
//			NeoAnalyzerMultiImpl analyzer = new NeoAnalyzerMultiImpl(true,true,true,true,true,true,true,true,true,true,numThreads);

			long start = System.currentTimeMillis();
			List<String> res = analyzer.analyze(g,false);
			long end = System.currentTimeMillis();
			System.out.println("starting to analyze:");
			
			for(String nodeProps : res){
				System.out.println(nodeProps);
			}

			durations.add((end-start));
		}
		g.shutdown();

		long sum = 0;
		for(Long duration : durations){
			sum += duration;
			System.out.println("duration\t"+duration);
		}
		System.out.println("average duration\t"+sum/(double)durations.size());
	}
}
