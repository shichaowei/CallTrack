package splab.ufcg.calltrack.model;

public enum TypeNode {
	NORMAL("Normal"), ARTIFACT_US("UseCase"), ARTIFACT_TC("TestCase");
	
	private final String value;
	
	private TypeNode(String value){
		this.value = value;
	}
	
	public String toString(){
		return this.value;
	}
}
