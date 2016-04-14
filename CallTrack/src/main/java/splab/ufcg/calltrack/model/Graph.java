package splab.ufcg.calltrack.model;

import java.util.Map;
import java.util.TreeMap;

import splab.ufcg.calltrack.exceptions.NodeNotFoundException;
import splab.ufcg.calltrack.model.Node;

public class Graph {
	
	private Map<String, Node> nodes = new TreeMap<String, Node>();
	
	
	public void putNode(String nodeId, String label, TypeNode type){
		if(!nodes.containsKey(nodeId)){
			nodes.put(nodeId, new Node(nodeId, label, type));
		}
	}


	public Map<String, Node> getNodes() {
		return nodes;
	}


	public void putEdge(String fromNodeId, String toNodeId) throws NodeNotFoundException {
		if(!nodes.containsKey(fromNodeId) )
			throw new NodeNotFoundException("The node " + fromNodeId + " was not found.");
		if(!nodes.containsKey(toNodeId))
			throw new NodeNotFoundException("The node " + toNodeId + " was not found.");
		
		Node fromNode = nodes.get(fromNodeId);
		Node toNode = nodes.get(toNodeId);
		
		fromNode.putEdge(toNode);
		
		
	}
	
}
