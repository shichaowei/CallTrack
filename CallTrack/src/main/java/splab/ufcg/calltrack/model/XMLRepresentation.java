package splab.ufcg.calltrack.model;

import java.util.List;

public class XMLRepresentation {
	private String id,name,onClickURL, type;
	private boolean openURL;
	
	
	
	
	
	public XMLRepresentation(String id, String name, String onClickURL, String type, boolean openURL, List<String> toIDs) {
		this.id = id;
		this.name = name;
		this.onClickURL = onClickURL;
		this.type = type;
		this.openURL = openURL;
		this.toIDs = toIDs;
	}
	
	
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}


	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public boolean isOpenURL() {
		return openURL;
	}

	public void setOpenURL(boolean openURL) {
		this.openURL = openURL;
	}
	private List<String> toIDs;
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	public String getOnClickURL() {
		return onClickURL;
	}
	public void setOnClickURL(String onClickURL) {
		this.onClickURL = onClickURL;
	}
	public List<String> getToIDs() {
		return toIDs;
	}
	public void setToIDs(List<String> toIDs) {
		this.toIDs = toIDs;
	}
	

}
