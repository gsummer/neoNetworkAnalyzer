package org.networklibrary.neonetworkanalyzer;

import java.util.ArrayList;
import java.util.List;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.factory.GraphDatabaseSettings;
import org.networklibrary.neonetworkanalyzer.neo4jalgos.NeoAnalyzerImpl;

/**
 * Hardcoded test space!!
 *
 */
public class App 
{
	public static void main( String[] args )
	{
		if(args.length != 2){
			System.out.println("not enough arguments; need graph and numRuns");
			return;
		}

		String graphloc = args[0];
		int numRuns = Integer.parseInt(args[1]);
		
		List<Long> durations = new ArrayList<Long>();
		
		GraphDatabaseService g = new GraphDatabaseFactory().newEmbeddedDatabase(graphloc);
		
//		GraphDatabaseService g = new GraphDatabaseFactory().newEmbeddedDatabaseBuilder(graphloc)
//				.setConfig(GraphDatabaseSettings.nodestore_mapped_memory_size,"400M")
//				.setConfig(GraphDatabaseSettings.relationshipstore_mapped_memory_size,"400M")
//				.newGraphDatabase();
		
		for(int i = 0; i < numRuns; ++i){

			System.out.println("starting to analyze:");
			NeoAnalyzerImpl analyzer = new NeoAnalyzerImpl();

			long start = System.currentTimeMillis();
			List<String> res = analyzer.analyze(g,true);
			long end = System.currentTimeMillis();

			//		for(String nodeProps : res){
			//			System.out.println(nodeProps);
			//		}

			System.out.println("duration\t" + (end - start));
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
