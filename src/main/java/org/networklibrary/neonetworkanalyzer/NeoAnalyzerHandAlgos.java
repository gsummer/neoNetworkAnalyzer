package org.networklibrary.neonetworkanalyzer;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.neo4j.graphdb.GraphDatabaseService;

public class NeoAnalyzerHandAlgos implements NeoAnalyzer{

	public NeoAnalyzerHandAlgos() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public List<String> analyze(GraphDatabaseService graph) {
		// TODO Auto-generated method stub
		return null;
	}

	String toJSON(Map<String,Object> nodeResults) throws JsonGenerationException, JsonMappingException, IOException{
		ObjectMapper m = new ObjectMapper();
		return m.writeValueAsString(nodeResults);
	}
}
