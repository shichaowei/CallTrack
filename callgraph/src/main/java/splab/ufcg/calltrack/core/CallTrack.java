/*
 * Copyright (c) 2011 - Georgios Gousios <gousiosg@gmail.com>
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *
 *     * Redistributions in binary form must reproduce the above
 *       copyright notice, this list of conditions and the following
 *       disclaimer in the documentation and/or other materials provided
 *       with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package splab.ufcg.calltrack.trackReflection;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;

import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.bcel.classfile.ClassFormatException;
import org.apache.bcel.classfile.ClassParser;

import splab.ufcg.calltrack.exceptions.NodeNotFoundException;
import splab.ufcg.calltrack.model.Graph;
import splab.ufcg.calltrack.model.Node;
import splab.ufcg.calltrack.model.TypeNode;
import splab.ufcg.calltrack.utils.Utils;

public class CallTrack {

	private String jarName;
	private String pattern;
	// private Graph2DView view;
	private ClassVisitor visitor;
	private Graph graphOfClass = new Graph();
	private Graph graphOfMethods = new Graph();
	private Utils util = new Utils();


	public CallTrack(String jarName, String pattern) {
		this.jarName = jarName;
		this.pattern = pattern;
		// graphClasses = new Graph2D();
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
		int count = 0;
		System.out.println(ClassVisitor.edges.size());
		for (String line : ClassVisitor.edges) {
			String[] nodesDeVided = line.split(" ");
			
			
			
			if (nodesDeVided[0].contains(":") && nodesDeVided[1].contains(":") && !nodesDeVided[0].equals(nodesDeVided[1]) && !(nodesDeVided[0].contains("clinit") || nodesDeVided[0].contains("clinit"))) {
				// Generating Label and creating "from Node"
				String fromNodeId = nodesDeVided[1];
				String[] nodeIdSplited = fromNodeId.split("\\.");
				String label = nodeIdSplited[nodeIdSplited.length - 1];
			//	Node fromNode = new Node(fromNodeId, label);
				graphOfMethods.putNode(fromNodeId, label, TypeNode.NORMAL);

				// Generating Label and creating "to Node"
				String toNodeId = nodesDeVided[0];
				nodeIdSplited = toNodeId.split("\\.");
				label = nodeIdSplited[nodeIdSplited.length - 1];
			//	Node toNode = new Node(toNodeId, label);
				graphOfMethods.putNode(toNodeId, label, TypeNode.NORMAL);
				//Generating Edge
				//Edge edge = new Edge(""+ count, fromNodeId, toNodeId);
				graphOfMethods.putEdge(fromNodeId, toNodeId);

				
				
			} else {
				// Generating Label and creating "from Node"
				String fromNodeId = nodesDeVided[1];
				String[] nodeIdSplited = fromNodeId.split("\\.");
				String label = nodeIdSplited[nodeIdSplited.length - 1];
//				Node fromNode = new Node(fromNodeId, label);
				graphOfClass.putNode(fromNodeId, label, TypeNode.NORMAL);

				// Generating Label and creating "to Node"
				String toNodeId = nodesDeVided[0];
				nodeIdSplited = toNodeId.split("\\.");
				label = nodeIdSplited[nodeIdSplited.length - 1];
//				Node toNode = new Node(toNodeId, label);
				graphOfClass.putNode(toNodeId, label, TypeNode.NORMAL);
	
				//Generating Edge
//				Edge edge = new Edge(""+ count, fromNodeId, toNodeId);
				graphOfClass.putEdge(fromNodeId, toNodeId);
				
			}
			count++;
		}
		
		//TODO Create all Artifacts Nodes from artifacts.conf and put in Graph
		
		
		//TODO Process all changes(Visit the nodes) and must be one graph to each change listed
		
		
		//TODO Transform each change graph to one DTO Graph that will be used to show in JS framework.
		
		
		
	
		

	}

	public Set<String> getEdgesSet() {
		return ClassVisitor.edges;
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
