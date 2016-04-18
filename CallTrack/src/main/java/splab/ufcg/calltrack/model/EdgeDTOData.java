package splab.ufcg.calltrack.model;

import com.google.gson.annotations.SerializedName;

public class EdgeDTOData {
	@SerializedName("@id@")
	private String id;
	@SerializedName("@source@")
	private String source;
	@SerializedName("@target@")
	private String target;
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getSource() {
		return source;
	}
	public void setSource(String source) {
		this.source = source;
	}
	public String getTo() {
		return target;
	}
	public void setTo(String to) {
		this.target = to;
	}
	public EdgeDTOData(String id, String source, String to) {
		setId(id);
		setSource(source);
		setTo(to);
	}
	
	
	
	
}
