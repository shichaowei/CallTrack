package gr.gousiosg.javacg.model;

public class Node{
	private String id;
	private String label;
	
	
	public Node(String id, String label) {
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
}