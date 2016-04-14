package splab.ufcg.calltrack.model;

import java.util.HashSet;
import java.util.Set;

public class Node{
	private String id;
	private String label;
	private TypeNode type;
	private Set<Node> edges = new HashSet<Node>();
	private boolean visited = false;
	
	public Node(String id, String label, TypeNode type) {
		super();
		setId(id);
		setLabel(label);
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	
	public boolean equals(Object obj){
		if(! (obj instanceof Node))
			return false;
		
		Node n = (Node) obj;
		
		return this.id.equals(n.getId());
	}
	
	public void putEdge(Node toNode){
		edges.add(toNode);
	}
	public TypeNode getType() {
		return type;
	}
	public void setType(TypeNode type) {
		this.type = type;
	}
	public Set<Node> getEdges() {
		return edges;
	}
	
	
	public void visit(){
		if(!this.visited){
			this.visited = true;
			for(Node n : edges){
				n.visit();
			}
		}
	}
	
}