package org.networklibrary.neonetworkanalyzer;

import java.util.List;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.server.plugins.Description;
import org.neo4j.server.plugins.Name;
import org.neo4j.server.plugins.PluginTarget;
import org.neo4j.server.plugins.ServerPlugin;
import org.neo4j.server.plugins.Source;

public class NeoAnalyzerExt extends ServerPlugin {
	@Name( "analyze" )
	@Description( "runs the whole network analyzer on the graph" )
	@PluginTarget( GraphDatabaseService.class )
	public Iterable<String> analyze( @Source GraphDatabaseService graph )
	{
		List<String> result = null;
		
		NeoAnalyzer analyzer = new NeoAnalyzer();
		result = analyzer.analyze(graph);
		
		return result;
	}
}
