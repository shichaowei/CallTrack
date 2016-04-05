package gr.gousiosg.javacg.model;

public class Edge{
	private String id;
	private String from;
	private String to;
	private String arrows = "to";
	
	public String getId() {
		return id;
	}
	public Edge(String id, String from, String to) {
		super();
		this.id = id;
		this.from = from;
		this.to = to;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getFrom() {
		return from;
	}
	public void setFrom(String from) {
		this.from = from;
	}
	public String getTo() {
		return to;
	}
	public void setTo(String to) {
		this.to = to;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof Node))
			return false;
		
		Edge external = (Edge) obj;
		
		return this.getFrom().equals(external.getFrom()) && this.getTo().equals(external.getTo());
		
	}
	
	public boolean isSelfLoop(){
		return this.getFrom().equals(this.getTo());
	}
	public String getArrows() {
		return arrows;
	}
	public void setArrows(String arrows) {
		this.arrows = arrows;
	}
}