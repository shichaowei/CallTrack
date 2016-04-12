package splab.ufcg.calltrack.model;

import java.util.ArrayList;
import java.util.List;

import splab.ufcg.calltrack.model.Edge;
import splab.ufcg.calltrack.model.Node;

public class Graph {
	private List<Node> nodes = new ArrayList<Node>();
	private List<Edge> edges = new ArrayList<Edge>();

	public List<Node> getNodes() {
		return nodes;
	}
	public void setNodes(List<Node> nodes) {
		this.nodes = nodes;
	}
	public List<Edge> getEdges() {
		return edges;
	}
	public void setEdges(List<Edge> edges) {
		this.edges = edges;
	}
	
	public boolean containsNode(Node node){
		return nodes.contains(node);
	}
	
	public boolean containsEdge(Edge edge){
		return edges.contains(edge);
	}
	
	public void putNode(Node node){
		nodes.add(node);
	}
	
	public void putEdge(Edge edge){
		edges.add(edge);
	}
}