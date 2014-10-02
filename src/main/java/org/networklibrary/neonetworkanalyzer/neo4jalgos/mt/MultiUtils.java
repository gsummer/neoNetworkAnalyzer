package org.networklibrary.neonetworkanalyzer.neo4jalgos.mt;

import java.util.Map;
import java.util.Map.Entry;

import org.neo4j.graphdb.Node;

public class MultiUtils {

	static public void addMappedDouble(Node n, Double value, Map<Node,Double> target){
		target.put(n, target.get(n) + value);
	}
	
	static public void mergeIntoMap(Map<Node,Double> target, Map<Node,Double> toMerge){
		for(Entry<Node,Double> t : target.entrySet()){
			addMappedDouble(t.getKey(), toMerge.get(t.getKey()), target);
		}
	}
}
