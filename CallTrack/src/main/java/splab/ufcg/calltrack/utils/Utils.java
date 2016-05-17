package splab.ufcg.calltrack.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import splab.ufcg.calltrack.model.EdgeBruteLine;
import splab.ufcg.calltrack.model.GraphDTO;
import splab.ufcg.calltrack.model.XMLRepresentation;

public class Utils {

	public void writeFile(String fileName, Map<String, List<EdgeBruteLine>> graphModel) {
		try {
			// String filename= "MyGraph.txt";
			String idContents = "";
			String edgeContents = "";
			FileWriter fw = new FileWriter(fileName, true); // the true will
															// append the new
															// data
			// fw.write(content + "\r\n");//appends the string to the file
			for (String id : graphModel.keySet()) {
				String[] nodeContent = id.split("\\.");
				System.out.println("Spliting " + id + " ...");
				idContents += id + " " + nodeContent[nodeContent.length - 1] + "\r\n";

				for (EdgeBruteLine edge : graphModel.get(id)) {
					edgeContents += edge.getFromID() + " " + edge.getToID() + " Calls\r\n";
				}

			}
			fw.write(idContents);
			fw.write("#\r\n");
			fw.write(edgeContents);

			fw.close();
		} catch (IOException e) {
			// TODO: handle exception
		}
	}

	public void writeJSONFile(String fileName, GraphDTO graph) {
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		String json = gson.toJson(graph);

		json = "data = " + json.replaceAll("\"@", "").replaceAll("@\"", "").replace("\"", "'");
		System.out.println(json);

		try {
			FileWriter fw = new FileWriter(fileName, true);
			fw.write(json);
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void deleteFiles(String filePath) {

		File f = new File(filePath);
		f.delete();

	}

	public List<XMLRepresentation> getArtifactsRepresentation(String artifactsFile) {
		File fXmlFile = new File(artifactsFile);
		List<XMLRepresentation> artifactsList = new LinkedList<XMLRepresentation>();

		try {

			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(fXmlFile);

			doc.getDocumentElement().normalize();
			

			NodeList nList = doc.getElementsByTagName("artifact");
			
			System.out.println(nList.getLength());
			
			for (int i = 0; i < nList.getLength(); i++) {
				Node node = nList.item(i);
				
				if (node.getNodeType() == Node.ELEMENT_NODE) {
					Element artifactElement = (Element) node;
					// XMLRepresentation artifact = new XML
				
					
					
					String artifactId = artifactElement.getAttribute("id");
					System.out.println("Artifact id : " + artifactId);
					
					String artifactType = artifactElement.getAttribute("type");
					System.out.println("Artifact type : " + artifactType);
					
					String artifactName = artifactElement.getElementsByTagName("name").item(0).getTextContent();
					System.out.println("First Name : " + artifactName);
					
					String openOnClick =  artifactElement.getAttribute("openUrlOnClick");
					System.out.println("Artifact openUrlOnClick : " + openOnClick);
					
					String onClickURL = "true".equals(openOnClick) && 
							artifactElement.getElementsByTagName("OnClickURL").getLength() > 0 ? artifactElement.getElementsByTagName("OnClickURL").item(0).getTextContent() 
									: "#"; 
					System.out.println("OnClickURL : " + onClickURL);
					
					
					System.out.println("Size of to from " + artifactName + " artifact: " + artifactElement.getElementsByTagName("to").getLength());
					
					Document toListDoc = artifactElement.getElementsByTagName("to").item(0).getOwnerDocument();
					
					
					Node child =  artifactElement.getElementsByTagName("to").item(0).getFirstChild();
					
					System.out.println("To nodes IDs:");
					List<String> toListValues = new LinkedList<String>();		
					while(child != null){
						if(child.getNodeType() == Node.ELEMENT_NODE){
							System.out.println("\t" + child.getTextContent());
							toListValues.add(child.getTextContent());
						}
						child = child.getNextSibling();
					}
					

					XMLRepresentation XMLNodeRepresentation = new XMLRepresentation(artifactId, artifactName, onClickURL, artifactType, "true".equals(openOnClick), toListValues);
					artifactsList.add(XMLNodeRepresentation);
					
					System.out.println();
					System.out.println();
					
					
					
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return artifactsList;
	}
	
	
	

}
