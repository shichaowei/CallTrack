package splab.ufcg.calltrack.model;

public class NodeDTO {
	private String id;
	private String label;
	private ColorHighlight color;
	
	public NodeDTO(String id, String label) {
		setId(id);
		setLabel(label);
		color = new ColorHighlight();
	}
	public ColorHighlight getColor() {
		return color;
	}
	public void setColor(ColorHighlight color) {
		this.color = color;
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
}
