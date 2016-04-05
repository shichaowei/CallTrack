package gr.gousiosg.javacg.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import gr.gousiosg.javacg.model.EdgeDTO;

public class Utils {

	public static void writeFile(String fileName, Map<String, List<EdgeDTO>> graphModel) {
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
	
	public static void writeFileAsJSON(String fileName, Map<String, List<EdgeDTO>> graphModel){
		//TODO
		
		
	}

	public static void deleteFiles(String filePath) {

		File f = new File(filePath);
		f.delete();

	}
	
	
	
}



