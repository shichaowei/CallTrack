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

package gr.gousiosg.javacg.stat;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;

import org.apache.bcel.classfile.ClassFormatException;
import org.apache.bcel.classfile.ClassParser;

import gr.gousiosg.javacg.model.EdgeDTO;
import gr.gousiosg.javacg.utils.Utils;
import y.base.Node;
import y.view.Graph2D;
import y.view.Graph2DView;

public class JCallGraph {

	private String jarName;
	private String pattern;
	// private Graph2DView view;
	private Graph2D graphClasses;

	private Map<String, List<EdgeDTO>> graphMapClasses = new HashMap<String, List<EdgeDTO>>();
	private Map<String, List<EdgeDTO>> graphMapMethods = new HashMap<String, List<EdgeDTO>>();

	public JCallGraph(String jarName, String pattern) {
		this.jarName = jarName;
		this.pattern = pattern;
		graphClasses = new Graph2D();
	}

	// public static void main(String[] args) {
	// ClassParser cp;
	// if(args.length >= 3){
	// File f = new File(args[0]);
	// if (!f.exists()) {
	// System.err.println("Jar file " + args[0] + " does not exist");
	// System.exit(-1);
	// }
	//
	// JarFile jar = null;
	// try {
	// jar = new JarFile(f);
	// } catch (IOException e1) {
	// System.err.println("Error while processing jar: " + e1.getMessage());
	// e1.printStackTrace();
	// System.exit(-1);
	// }
	//
	// Utils.deleteFiles(args[2]);
	//
	// Enumeration<JarEntry> entries = jar.entries();
	// while (entries.hasMoreElements()) {
	// JarEntry entry = entries.nextElement();
	// if (entry.isDirectory())
	// continue;
	//
	// if (!entry.getName().endsWith(".class"))
	// continue;
	//
	// cp = new ClassParser(args[0],entry.getName());
	// ClassVisitor visitor;
	// try {
	// visitor = new ClassVisitor(cp.parse(),args[1];
	// visitor.start();
	// } catch (ClassFormatException e) {
	// System.err.println("Error while processing jar: " + e.getMessage());
	// e.printStackTrace();
	// } catch (IOException e) {
	// System.err.println("Error while processing jar: " + e.getMessage());
	// e.printStackTrace();
	// }
	// }
	//
	//
	//
	// }else{
	// System.err.println("Params error. You have to execute \"java -jar
	// projectToAnalyze.jar pattern outputFilePrefix\"");
	// System.exit(-1);
	//
	// }
	//
	//
	// }

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
			ClassVisitor visitor;
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

	public void processInput() {
		for (String line : ClassVisitor.edges) {
			String[] nodesDeVided = line.split(" ");

			if (nodesDeVided[0].contains(":") && nodesDeVided[1].contains(":")) {
				System.out.println(line + "[method-to-method]");
				String fromNode = nodesDeVided[1];
				String toNode = nodesDeVided[0];

				if (!graphMapMethods.containsKey(fromNode)) {
					EdgeDTO edge = new EdgeDTO(fromNode, toNode);
					List<EdgeDTO> list = new ArrayList<EdgeDTO>();
					list.add(edge);
					graphMapMethods.put(fromNode, list);
				} else {
					EdgeDTO edge = new EdgeDTO(fromNode, toNode);
					graphMapMethods.get(fromNode).add(edge);
				}

				if (!graphMapMethods.containsKey(toNode)) {
					EdgeDTO edge = new EdgeDTO(fromNode, toNode);
					List<EdgeDTO> list = new ArrayList<EdgeDTO>();
					graphMapMethods.put(fromNode, list);
				}

			} else {
				System.out.println(line + "[class-to-class]");

				String fromNode = nodesDeVided[1];
				String toNode = nodesDeVided[0];

				if (!graphMapClasses.containsKey(fromNode)) {
					EdgeDTO edge = new EdgeDTO(fromNode, toNode);
					List<EdgeDTO> list = new ArrayList<EdgeDTO>();
					list.add(edge);
					graphMapClasses.put(fromNode, list);
				} else {
					EdgeDTO edge = new EdgeDTO(fromNode, toNode);
					graphMapClasses.get(fromNode).add(edge);
				}

				if (!graphMapClasses.containsKey(toNode)) {
					EdgeDTO edge = new EdgeDTO(fromNode, toNode);
					List<EdgeDTO> list = new ArrayList<EdgeDTO>();
					graphMapClasses.put(fromNode, list);
				}

				// Node n1 = this.graph.createNode(100, 100, nodesDeVided[1] ==
				// null? "Null Node" : nodesDeVided[1]);
				// Node n2 = this.graph.createNode(100, 100, nodesDeVided[1] ==
				// null? "Null Node" : nodesDeVided[1]);

				// graph.createEdge(n1,n2);

			}
		}
		String pathTestFileClasses = "C:\\Users\\walter\\workspace\\callgraph\\Exemplo_grafo-classes.tgf";
		String pathTestFileMethods = "C:\\Users\\walter\\workspace\\callgraph\\Exemplo_grafo-methods.tgf";

		Utils.deleteFiles(pathTestFileClasses);
		Utils.deleteFiles(pathTestFileMethods);

		Utils.writeFile(pathTestFileClasses, graphMapClasses);
		Utils.writeFile(pathTestFileMethods, graphMapMethods);

	}

	public Graph2D getGraph() {
		return this.graphClasses;
	}

	public Set<String> getEdgesSet() {
		return ClassVisitor.edges;
	}

	public static void main(String[] args) {
		JCallGraph cg = new JCallGraph(args[0], args[1]);
		cg.prepare();
		cg.processInput();

	}

}
