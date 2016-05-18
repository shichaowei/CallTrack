package splab.ufcg.calltrack.model;

import java.awt.Window.Type;

import com.google.gson.annotations.SerializedName;

public class NodeDTOData {
	@SerializedName("@id@")
	private String id;
	@SerializedName("@label@")
	private String label;
	@SerializedName("@type@")
	private String type;
	@SerializedName("@shape@")
	private String shape;
	@SerializedName("@color@")
	private String backgroundColor;
	@SerializedName("@colorHighlight@")
	private String colorHighlight;
	
	public NodeDTOData(String id, String label, TypeNode type) {
		setId(id);
		setLabel(label);
		setType(type);
		setShape(type);
		setBackgroundColor(type);
		setColorHighlight(type);
	}
	
	
	public String getShape() {
		return shape;
	}
	private void setShape(TypeNode type){
		if(type == TypeNode.ARTIFACT_US)
			this.shape = "rectangle";
		else if(type == TypeNode.ARTIFACT_TC)
			this.shape = "triangle";
		else
			this.shape = "ellipse";
			
	}
	
	public String getType() {
		return type;
	}
	public void setType(TypeNode type) {
		this.type = type.toString();
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


	public String getBackgroundColor() {
		return backgroundColor;
	}


	private void setBackgroundColor(TypeNode type) {
		if(type == TypeNode.ARTIFACT_US)
			this.backgroundColor = "#FF9900";
		else if(type == TypeNode.ARTIFACT_TC)
			this.backgroundColor = "#FF9900";
		else
			this.backgroundColor ="#A0A0A0";
	}


	public String getColorHighlight() {
		return colorHighlight;
	}


	private void setColorHighlight(TypeNode type) {
		if(type == TypeNode.ARTIFACT_US || type == TypeNode.ARTIFACT_TC)
			this.colorHighlight = "#FF0000";
		else
			this.colorHighlight ="#61bffc";
	}
	
	
	
	

}
