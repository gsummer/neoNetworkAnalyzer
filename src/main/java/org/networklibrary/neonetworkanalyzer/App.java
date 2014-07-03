package org.networklibrary.neonetworkanalyzer;

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
		// mac os x
		//    	String testing = "/Users/gsu/random/cynetlibsync/neo4j-community-2.1.1/data/graph.db";
//		String testing = "/Users/gsu/random/cynetlibsync/neo4j-community-2.1.1-2/data/graph.db";
		//        testing = "/Users/gsu/random/cynetlibsync/neo4j-community-2.0.3_testing/data/graph.db";


		// proper OS

		//        final String testing = "/home/gsu/random/neoanalyzer/testing/data/graph.db";
		//        final String full = "/home/gsu/random/neoanalyzer/full/data/graph.db";
		
		String testing = "/home/gsu/random/benchmarking/neo4j-community-2.1.2/data/graph.db";

		GraphDatabaseService g = new GraphDatabaseFactory().newEmbeddedDatabase(testing);

		System.out.println("starting to analyze:");
		NeoAnalyzerImpl analyzer = new NeoAnalyzerImpl();

		long start = System.currentTimeMillis();
		List<String> res = analyzer.analyze(g,true);
		long end = System.currentTimeMillis();

		System.out.println("duration: " + (end - start));

		for(String nodeProps : res){
			System.out.println(nodeProps);
		}
	}
}
