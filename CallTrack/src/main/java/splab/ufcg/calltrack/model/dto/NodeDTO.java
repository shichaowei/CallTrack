package splab.ufcg.calltrack.model.dto;

import com.google.gson.annotations.SerializedName;

public class NodeDTO {
	@SerializedName("@data@")
	private NodeDTOData data;

	public NodeDTOData getData() {
		return data;
	}

	public void setData(NodeDTOData data) {
		this.data = data;
	}

	public NodeDTO(NodeDTOData data) {
		this.data = data;
	}
	
	
	
	
}
