package org.networklibrary.neonetworkanalyzer;

import java.util.List;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.server.plugins.Description;
import org.neo4j.server.plugins.Name;
import org.neo4j.server.plugins.Parameter;
import org.neo4j.server.plugins.PluginTarget;
import org.neo4j.server.plugins.ServerPlugin;
import org.neo4j.server.plugins.Source;
import org.networklibrary.neonetworkanalyzer.neo4jalgos.NeoAnalyzerImpl;

public class NeoAnalyzerExt extends ServerPlugin {
	@Name( "neonetworkanalyzer" )
	@Description( "runs the whole network analyzer on the graph" )
	@PluginTarget( GraphDatabaseService.class )
	public Iterable<String> neonetworkanalyzer( @Source GraphDatabaseService graph,
			@Description( "flag to indicate if the statistics should be stored in the graph database" )
			@Parameter( name = "saveInGraph", optional = false ) boolean saveInGraph)
	{
		List<String> result = null;

		NeoAnalyzer analyzer = new NeoAnalyzerImpl();
		result = analyzer.analyze(graph,saveInGraph);

		return result;
	}
}
