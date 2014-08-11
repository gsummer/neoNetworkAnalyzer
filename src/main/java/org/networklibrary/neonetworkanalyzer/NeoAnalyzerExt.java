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
			@Parameter( name = "saveInGraph", optional = false ) boolean saveInGraph,
			@Description( "flag to indicate if the eccentricity should be calculated" )
			@Parameter( name = "eccentricity", optional = false ) boolean eccentricity,
			@Description( "flag to indicate if the betweenness should be calculated" )
			@Parameter( name = "betweenness", optional = false ) boolean betweenness,
			@Description( "flag to indicate if the stress should be calculated" )
			@Parameter( name = "stress", optional = false ) boolean stress,
			@Description( "flag to indicate if the avgSP should be calculated" )
			@Parameter( name = "avgSP", optional = false ) boolean avgSP,
			@Description( "flag to indicate if the radiality should be calculated" )
			@Parameter( name = "radiality", optional = false ) boolean radiality,
			@Description( "flag to indicate if the topoCoeff should be calculated" )
			@Parameter( name = "topoCoeff", optional = false ) boolean topoCoeff,
			@Description( "flag to indicate if the neighbourhood should be calculated" )
			@Parameter( name = "neighbourhood", optional = false ) boolean neighbourhood,
			@Description( "flag to indicate if the multiEdgePairs should be calculated" )
			@Parameter( name = "multiEdgePairs", optional = false ) boolean multiEdgePairs,
			@Description( "flag to indicate if the closeness should be calculated" )
			@Parameter( name = "closeness", optional = false ) boolean closeness,
			@Description( "flag to indicate if the clustCoeff should be calculated" )
			@Parameter( name = "clustCoeff", optional = false ) boolean clustCoeff)
	{
		List<String> result = null;

		NeoAnalyzer analyzer = new NeoAnalyzerImpl(eccentricity,betweenness,stress,avgSP,radiality,topoCoeff,neighbourhood,multiEdgePairs,closeness,clustCoeff);
		result = analyzer.analyze(graph,saveInGraph);

		return result;
	}
}
