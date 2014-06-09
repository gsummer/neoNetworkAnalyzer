package org.networklibrary.neonetworkanalyzer;

import java.util.List;

import org.neo4j.graphdb.GraphDatabaseService;

public interface NeoAnalyzer {

	List<String> analyze(GraphDatabaseService graph);
	
}
