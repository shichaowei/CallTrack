package splab.ufcg.calltrack.core;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.bcel.classfile.ClassFormatException;
import org.apache.bcel.classfile.ClassParser;

import splab.ufcg.calltrack.exceptions.NodeNotFoundException;
import splab.ufcg.calltrack.model.Graph;
import splab.ufcg.calltrack.model.TypeNode;
import splab.ufcg.calltrack.model.XMLRepresentation;
import splab.ufcg.calltrack.utils.Utils;

public class CallTrack {

	private String jarName;
	private String pattern;
	private ClassVisitor visitor;
	private Graph graphOfClass = new Graph();
	private Graph graphOfMethods = new Graph();

	public CallTrack(String jarName, String pattern) {
		this.jarName = jarName;
		this.pattern = pattern;
	}

	public void prepare() {
		ClassParser cp;

		File f = new File(this.jarName);
		if (!f.exists()) {
			System.err.println("Jar file " + this.jarName + " does not exist");
			System.exit(-1);
		}

		JarFile jar = null;

		try {
			jar = new JarFile(f);
		} catch (IOException e1) {
			System.err.println("Error while processing jar: " + e1.getMessage());
			e1.printStackTrace();
			System.exit(-1);
		}

		Enumeration<JarEntry> entries = jar.entries();
		while (entries.hasMoreElements()) {
			JarEntry entry = entries.nextElement();
			if (entry.isDirectory())
				continue;

			if (!entry.getName().endsWith(".class"))
				continue;

			cp = new ClassParser(this.jarName, entry.getName());

			try {
				visitor = new ClassVisitor(cp.parse(), this.pattern);

				visitor.start();
			} catch (ClassFormatException e) {
				System.err.println("Error while processing jar: " + e.getMessage());
				e.printStackTrace();
			} catch (IOException e) {
				System.err.println("Error while processing jar: " + e.getMessage());
				e.printStackTrace();
			}
		}

	}

	public void processInput() throws NodeNotFoundException {
		for (String line : ClassVisitor.edgesMethods) {
			String[] nodesDeVided = line.split(" ");

			if (nodesDeVided[0].contains(":") && nodesDeVided[1].contains(":")
					&& !nodesDeVided[0].equals(nodesDeVided[1])
					&& !(nodesDeVided[0].contains("clinit") || nodesDeVided[0].contains("clinit"))) {
				// Generating Label and creating "from Node"
				String fromNodeId = nodesDeVided[1];
				String[] nodeIdSplited = fromNodeId.split("\\.");
				String label = nodeIdSplited[nodeIdSplited.length - 1];
				graphOfMethods.putNode(fromNodeId, label, TypeNode.NORMAL);

				// Generating Label and creating "to Node"
				String toNodeId = nodesDeVided[0];
				nodeIdSplited = toNodeId.split("\\.");
				label = nodeIdSplited[nodeIdSplited.length - 1];
				graphOfMethods.putNode(toNodeId, label, TypeNode.NORMAL);

				// Generating Edge
				if(!fromNodeId.equals(toNodeId))
					graphOfMethods.putEdge(fromNodeId, toNodeId);

			} 
		}
		
		
		for(String line : ClassVisitor.edgesClass){
			String[] nodesDeVided = line.split(" ");
			// Generating Label and creating "from Node"
			String fromNodeId = nodesDeVided[1];
			String[] nodeIdSplited = fromNodeId.split("\\.");
			String label = nodeIdSplited[nodeIdSplited.length - 1];
			// Node fromNode = new Node(fromNodeId, label);
			graphOfClass.putNode(fromNodeId, label, TypeNode.NORMAL);

			// Generating Label and creating "to Node"
			String toNodeId = nodesDeVided[0];
			nodeIdSplited = toNodeId.split("\\.");
			label = nodeIdSplited[nodeIdSplited.length - 1];
			graphOfClass.putNode(toNodeId, label, TypeNode.NORMAL);

			// Generating Edge
			if(!fromNodeId.equals(toNodeId))
				graphOfClass.putEdge(fromNodeId, toNodeId);
		}

		Utils util = new Utils();
		util.deleteFiles("view/data-class.json");
		util.deleteFiles("view/data-method.json");
		
		
		List<XMLRepresentation> classArtifactsRepresentation = util.getArtifactsRepresentation("conf\\artifacts-to-class.xml");
		
		for(XMLRepresentation representation : classArtifactsRepresentation){
			TypeNode type;
			if("UseCase".equals(representation.getType())){
				type = TypeNode.ARTIFACT_US;
			}else if("TestCase".equals(representation.getType())){
				type = TypeNode.ARTIFACT_TC;
			}else{
				type = TypeNode.NORMAL;
			}
			
			
			
			graphOfClass.putNode(representation.getId(), representation.getName(), type);
			
			for(String toId : representation.getToIDs()){
				if(!representation.getId().equals(toId))
					graphOfClass.putEdge(toId, representation.getId());
			}
			
			
			
		}
		util.writeJSONFile("view/data-class.json", graphOfClass.getGraphDTO());
		
		
		List<XMLRepresentation> methodArtifactsRepresentation = util.getArtifactsRepresentation("conf\\artifacts-to-methods.xml");
		
		for(XMLRepresentation representation : methodArtifactsRepresentation){
			TypeNode type;
			if("UseCase".equals(representation.getType())){
				type = TypeNode.ARTIFACT_US;
			}else if("TestCase".equals(representation.getType())){
				type = TypeNode.ARTIFACT_TC;
			}else{
				type = TypeNode.NORMAL;
			}
			
			graphOfMethods.putNode(representation.getId(), representation.getName(), type);
			
			for(String toId : representation.getToIDs()){
				if(!representation.getId().equals(toId))
					graphOfMethods.putEdge(toId, representation.getId());
			}
			
			
			
		}
		util.writeJSONFile("view/data-method.json", graphOfMethods.getGraphDTO());
		
		
		
		File fClass = new File("view/index-class.html");
		File fMethod = new File("view/index-method.html");
		try {
			Desktop.getDesktop().browse(fClass.toURI());
			Desktop.getDesktop().browse(fMethod.toURI());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// TODO Create all Artifacts Nodes from method-mapping.artifacs and
		// classes-mapping.artifacts and after put in Graph and link with
		// referenced node.

		// TODO Process all changes(Visit the nodes) and must be one graph to
		// each change listed

		// TODO Transform each change graph to one DTO Graph that will be used
		// to show in JS framework.

	}

	public Set<String> getEdgesSet() {
		return ClassVisitor.edgesMethods;
	}

	public static void main(String[] args) {
		CallTrack cg = new CallTrack(args[0], args[1]);
		cg.prepare();
		try {
			cg.processInput();
		} catch (NodeNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
