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
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;

import org.apache.bcel.classfile.ClassFormatException;
import org.apache.bcel.classfile.ClassParser;

import y.view.Graph2D;
import y.view.Graph2DView;

public class JCallGraph {

	private String jarName;
	private String pattern;
	Graph2DView view;
	Graph2D graph;

	public JCallGraph(String jarName, String pattern) {
		this.jarName = jarName;
		this.pattern = pattern;

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

	public static void main(String[] args) {
		JCallGraph cg = new JCallGraph(args[0], args[1]);
		cg.prepare();
		for (String line : ClassVisitor.edges) {
			if (Pattern
					.compile(
							"([a-z][a-z_0-9]*\\.)*[A-Z_]($[A-Z_]|[\\w_])*:[\\w_]* ([a-z][a-z_0-9]*\\.)*[A-Z_]($[A-Z_]|[\\w_])*:[\\w_]*")
					.matcher(line).find()) {
				System.out.println(line + "[method-to-method]");
				String[] nodesDeVided = line.split(" ");

			} else {
				System.out.println(line + "[class-to-class]");
			}
		}

	}

}
