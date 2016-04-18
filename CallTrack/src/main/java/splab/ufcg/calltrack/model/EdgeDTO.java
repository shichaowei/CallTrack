package splab.ufcg.calltrack.model;

import com.google.gson.annotations.SerializedName;

public class EdgeDTO{
	@SerializedName("@data@")
	EdgeDTOData data;

	public EdgeDTO(EdgeDTOData data) {
		this.data = data;
	}
	
	
	
}