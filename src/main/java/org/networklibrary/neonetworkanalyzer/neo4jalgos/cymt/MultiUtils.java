package org.networklibrary.neonetworkanalyzer.neo4jalgos.cymt;

import java.util.Map;
import java.util.Map.Entry;

import org.neo4j.graphdb.Node;

public class MultiUtils {

	static public void addMapped(Node n, Double value, Map<Node,Double> target){
		if(target != null)
			target.put(n, target.get(n) + value);
		
	}
	
	static public void addMapped(Node n, Long value, Map<Node,Long> target){
		target.put(n, target.get(n) + value);
	}
	
	static public void mergeIntoMapD(Map<Node,Double> target, Map<Node,Double> toMerge){
		for(Entry<Node,?> t : target.entrySet()){
			addMapped(t.getKey(), toMerge.get(t.getKey()), target);
		}
	}
	
	static public void mergeIntoMapL(Map<Node,Long> target, Map<Node,Long> toMerge){
		for(Entry<Node,?> t : target.entrySet()){
			addMapped(t.getKey(), toMerge.get(t.getKey()), target);
		}
	}

}
