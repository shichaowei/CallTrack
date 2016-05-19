package splab.ufcg.calltrack.model;

import java.util.Map;
import java.util.TreeMap;

import splab.ufcg.calltrack.exceptions.NodeNotFoundException;
import splab.ufcg.calltrack.model.Node;
import splab.ufcg.calltrack.model.dto.EdgeDTO;
import splab.ufcg.calltrack.model.dto.EdgeDTOData;
import splab.ufcg.calltrack.model.dto.GraphDTO;
import splab.ufcg.calltrack.model.dto.NodeDTO;
import splab.ufcg.calltrack.model.dto.NodeDTOData;

public class Graph {
	
	private Map<String, Node> nodes = new TreeMap<String, Node>();
	
	
	public void putNode(String nodeId, String label, TypeNode type){
		if(!nodes.containsKey(nodeId)){
			if(type == TypeNode.ARTIFACT_US)
				System.out.println("Adding " + nodeId);
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
		toNode.visit();
		fromNode.putEdge(toNode);
		
		
	}
	
	public GraphDTO getGraphDTO(){
		GraphDTO transactionDataGraph = new GraphDTO();
		
		for(Node n : this.nodes.values()){
			
			if(!n.isVisited() && n.getEdges().size() < 1)
				continue;
			
			NodeDTOData nodeData = new NodeDTOData(n.hashCode() + "", n.getLabel(), n.getType());
			NodeDTO node = new NodeDTO(nodeData);
			transactionDataGraph.putNode(node);
			
			for(Node e : n.getEdges()){
				EdgeDTOData edgeData = new EdgeDTOData(n.hashCode()+ "-" + e.hashCode(), n.hashCode() + "", e.hashCode() + "");
				EdgeDTO edge = new EdgeDTO(edgeData);
				transactionDataGraph.putEdge(edge);
			}
			
		}
		
		return transactionDataGraph;
	}
	
}
