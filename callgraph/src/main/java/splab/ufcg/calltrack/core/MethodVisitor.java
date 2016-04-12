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

package splab.ufcg.calltrack.core;

import java.util.ArrayList;
import java.util.List;

import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.ConstantPushInstruction;
import org.apache.bcel.generic.EmptyVisitor;
import org.apache.bcel.generic.FieldOrMethod;
import org.apache.bcel.generic.INVOKEINTERFACE;
import org.apache.bcel.generic.INVOKESPECIAL;
import org.apache.bcel.generic.INVOKESTATIC;
import org.apache.bcel.generic.INVOKEVIRTUAL;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionConstants;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.NEW;
import org.apache.bcel.generic.ReturnInstruction;

/**
 * The simplest of method visitors, prints any invoked method
 * signature for all method invocations.
 * 
 * Class copied with modifications from CJKM: http://www.spinellis.gr/sw/ckjm/
 */
public class MethodVisitor extends EmptyVisitor {

    JavaClass visitedClass;
    private MethodGen mg;
    private ConstantPoolGen cp;
    private String format;
    private String pattern;
    private List<String> peers;
    
    public MethodVisitor(MethodGen m, JavaClass jc,String pattern) {
        visitedClass = jc;
        mg = m;
        cp = mg.getConstantPool();
        format = visitedClass.getClassName() + ":" + mg.getName() 
            + " " + "%s:%s";
        this.pattern = pattern;
        this.peers = new ArrayList<String>();
    }

    public List<String> start() {
        if (mg.isAbstract() || mg.isNative())
            return null;
        for (InstructionHandle ih = mg.getInstructionList().getStart(); 
                ih != null; ih = ih.getNext()) {
            Instruction i = ih.getInstruction();
            
            if (!visitInstruction(i))
                i.accept(this);
        }
        
        return peers;
    }

    private boolean visitInstruction(Instruction i) {
        short opcode = i.getOpcode();

        return ((InstructionConstants.INSTRUCTIONS[opcode] != null)
                //&& !(i instanceof ConstantPushInstruction) 
                && !(i instanceof ReturnInstruction));
    }

    @Override
    public void visitINVOKEVIRTUAL(INVOKEVIRTUAL i) {
    	String output = String.format(format,i.getReferenceType(cp),i.getMethodName(cp)).replaceAll("[$\\d]+", "");;
    	if(i.getReferenceType(cp).toString().contains(this.pattern) ){
    		ClassVisitor.edges.add(output);
    	}
    }

    @Override
    public void visitINVOKEINTERFACE(INVOKEINTERFACE i) {
    	String output = String.format(format,i.getReferenceType(cp),i.getMethodName(cp)).replaceAll("[$\\d]+", "");;
    	if(i.getReferenceType(cp).toString().contains(this.pattern) ){
    		
    		ClassVisitor.edges.add(output);
    	}
    }

    @Override
    public void visitINVOKESPECIAL(INVOKESPECIAL i) {
    	String output = String.format(format,i.getReferenceType(cp),i.getMethodName(cp)).replaceAll("[$\\d]+", "");;
    	if(i.getReferenceType(cp).toString().contains(this.pattern) ){ 		
    		ClassVisitor.edges.add(output);
    	}
    }

    @Override
    public void visitINVOKESTATIC(INVOKESTATIC i) {
    	String output = String.format(format,i.getReferenceType(cp),i.getMethodName(cp)).replaceAll("[$\\d]+", "");;
    	if(i.getReferenceType(cp).toString().contains(this.pattern) ){
    		ClassVisitor.edges.add(output);
    	}
    }
    

}
