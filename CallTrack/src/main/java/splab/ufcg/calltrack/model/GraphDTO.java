package splab.ufcg.calltrack.model;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.SerializedName;

public class GraphDTO {
	@SerializedName("@nodes@")
	private List<NodeDTO> nodes;
	@SerializedName("@edges@")
	private List<EdgeDTO> edges;

	
	public GraphDTO() {
		nodes = new ArrayList<NodeDTO>();
		edges = new ArrayList<EdgeDTO>();
	}
	
	
	public List<NodeDTO> getNodes() {
		return nodes;
	}
	public List<EdgeDTO> getEdges() {
		return edges;
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
