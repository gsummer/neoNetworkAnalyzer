package org.networklibrary.neonetworkanalyzer;

import java.util.List;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.factory.GraphDatabaseSettings;

/**
 * Hardcoded test space!!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
    	final String testing = "/Users/gsu/random/cynetlibsync/neo4j-community-2.0.3_testing/data/graph.db";
        final String dbloc = "/Users/gsu/random/cynetlibsync/neo4j-community-2.0.3/data/graph.db";
        final String full = "/Users/gsu/random/netlib/full_import_test/data/graph.db";
        
        GraphDatabaseService g = new GraphDatabaseFactory().newEmbeddedDatabaseBuilder(
                testing )
                .setConfig( GraphDatabaseSettings.read_only, "true" )
                .newGraphDatabase();
//        GraphDatabaseService g = new GraphDatabaseFactory().newEmbeddedDatabase(dbloc);
//        GraphDatabaseService g = new GraphDatabaseFactory().newEmbeddedDatabase(full);
        
        System.out.println("starting to analyze:");
        NeoAnalyzer analyzer = new NeoAnalyzer();
        
        long start = System.currentTimeMillis();
        List<String> res = analyzer.analyze(g);
        long end = System.currentTimeMillis();
        
        System.out.println("duration: " + (end - start));
        
        for(String nodeProps : res){
        	System.out.println(nodeProps);
        }
    }
}
