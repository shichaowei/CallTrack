package splab.ufcg.calltrack.model;

import java.util.List;

public class GraphDTO {
	private List<NodeDTO> nodes;
	private List<EdgeDTO> edges;

	public List<NodeDTO> getNodes() {
		return nodes;
	}
	public void setNodes(List<NodeDTO> nodes) {
		this.nodes = nodes;
	}
	public List<EdgeDTO> getEdges() {
		return edges;
	}
	public void setEdges(List<EdgeDTO> edges) {
		this.edges = edges;
	}
	
	public boolean containsNode(Node node){
		return nodes.contains(node);
	}
	
	public boolean containsEdge(EdgeDTO edge){
		return edges.contains(edge);
	}
	
	public void putNode(NodeDTO node){
		nodes.add(node);
	}
	
	public void putEdge(EdgeDTO edge){
		edges.add(edge);
	}
}
