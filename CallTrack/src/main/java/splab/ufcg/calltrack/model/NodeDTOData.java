package splab.ufcg.calltrack.model;

import com.google.gson.annotations.SerializedName;

public class NodeDTOData {
	@SerializedName("@id@")
	private String id;
	@SerializedName("@label@")
	private String label;
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
	public NodeDTOData(String id, String label) {
		setId(id);
		setLabel(label);
	}
	
	
}
