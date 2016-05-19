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
    private MethodGen methodGen;
    private ConstantPoolGen cp;
    private String format;
    private String pattern;
    private List<String> peers;
    
    public MethodVisitor(MethodGen mGen, JavaClass javaClass,String pattern) {
        visitedClass = javaClass;
        methodGen = mGen;
        cp = methodGen.getConstantPool();
        format = visitedClass.getClassName() + ":" + methodGen.getName() 
            + " " + "%s:%s";
        this.pattern = pattern;
        this.peers = new ArrayList<String>();
    }

    public List<String> start() {
        if (methodGen.isAbstract() || methodGen.isNative())
            return null;
        for (InstructionHandle ih = methodGen.getInstructionList().getStart(); 
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
    	String output = String.format(format,i.getReferenceType(cp),i.getMethodName(cp));
    	if(i.getReferenceType(cp).toString().contains(this.pattern) ){
    		ClassVisitor.edgesMethods.add(output);
    	}
    }

    @Override
    public void visitINVOKEINTERFACE(INVOKEINTERFACE i) {
    	String output = String.format(format,i.getReferenceType(cp),i.getMethodName(cp));
    	if(i.getReferenceType(cp).toString().contains(this.pattern) ){
    		
    		ClassVisitor.edgesMethods.add(output);
    	}
    }

    @Override
    public void visitINVOKESPECIAL(INVOKESPECIAL i) {
    	String output = String.format(format,i.getReferenceType(cp),i.getMethodName(cp));
    	if(i.getReferenceType(cp).toString().contains(this.pattern) ){ 		
    		ClassVisitor.edgesMethods.add(output);
    	}
    }

    @Override
    public void visitINVOKESTATIC(INVOKESTATIC i) {
    	String output = String.format(format,i.getReferenceType(cp),i.getMethodName(cp));
    	if(i.getReferenceType(cp).toString().contains(this.pattern) ){
    		ClassVisitor.edgesMethods.add(output);
    	}
    }
    

}
