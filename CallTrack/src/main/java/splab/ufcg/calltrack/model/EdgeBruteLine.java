package splab.ufcg.calltrack.model;

public class EdgeBruteLine{
	private String fromID,toID;
	
	public EdgeBruteLine(String fromID,String toID) {
		this.fromID = fromID;
		this.toID = toID;
	}

	public String getFromID() {
		return fromID;
	}

	public void setFromID(String fromID) {
		this.fromID = fromID;
	}

	public String getToID() {
		return toID;
	}

	public void setToID(String toID) {
		this.toID = toID;
	}

	public boolean haveNode(String nodeId){
		return this.fromID.equals(nodeId) || this.toID.equals(nodeId);
	}
	
}