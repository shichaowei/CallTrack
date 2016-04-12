package splab.ufcg.calltrack.model;

public class ColorHighlight extends Color {
	private Color highlight;
	
	public ColorHighlight() {
		this.highlight = new Color();
		this.highlight.setBackground("#ED181B");
		this.highlight.setBorder("#FF0000");
	}
	
	public Color getHighlight() {
		return highlight;
	}

	public void setHighlight(Color highlight) {
		this.highlight = highlight;
	}
	
}
