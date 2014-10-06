package org.networklibrary.neonetworkanalyzer;

import java.util.ArrayList;
import java.util.List;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.factory.GraphDatabaseSettings;
import org.networklibrary.neonetworkanalyzer.neo4jalgos.NeoAnalyzerImpl;
import org.networklibrary.neonetworkanalyzer.neo4jalgos.NeoAnalyzerMultiImpl;

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

		for(int i = 0; i < numRuns; ++i){


//			NeoAnalyzerImpl analyzer = new NeoAnalyzerImpl(true,false,true,false,false,false,false,false,false,false);
			NeoAnalyzerMultiImpl analyzer = new NeoAnalyzerMultiImpl(true,false,true,false,false,false,false,false,false,false);

			long start = System.currentTimeMillis();
			List<String> res = analyzer.analyze(g,true);
			long end = System.currentTimeMillis();
			System.out.println("starting to analyze:");
			
			//		NeoAnalyzerImpl analyzer = new NeoAnalyzerImpl(true,true,true,true,true,true,true,true,true,true);

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
