package gr.gousiosg.javacg.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Utils {
	
	
	public static void witeFile(String fileName, String content){
		try{
    		//String filename= "MyGraph.txt";
			
    	    FileWriter fw = new FileWriter(fileName,true); //the true will append the new data
    	    fw.write(content + "\r\n");//appends the string to the file
    	    fw.close();
    }catch (IOException e) {
		// TODO: handle exception
	}
	}
	
	public static void deleteFiles(String prefixFile){
		new File(prefixFile + "-class.txt").delete();
		new File(prefixFile + "-method.txt").delete();
	}
}
