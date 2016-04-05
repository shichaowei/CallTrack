package gr.gousiosg.javacg.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import gr.gousiosg.javacg.model.EdgeDTO;
import gr.gousiosg.javacg.model.Graph;

public class Utils {

	public void writeFile(String fileName, Map<String, List<EdgeDTO>> graphModel) {
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

				for (EdgeDTO edge : graphModel.get(id)) {
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
	
	public void writeJSONFile(String fileName, Graph graph){
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		String json = gson.toJson(graph);
		System.out.println(json);
		
		json = "data = " + json;
		
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
	
	
	
}



