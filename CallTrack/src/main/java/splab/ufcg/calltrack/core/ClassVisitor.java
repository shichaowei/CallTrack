package splab.ufcg.calltrack.core;

import java.util.HashSet;
import java.util.Set;

import org.apache.bcel.classfile.Constant;
import org.apache.bcel.classfile.ConstantPool;
import org.apache.bcel.classfile.EmptyVisitor;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.MethodGen;

/**
 * The simplest of class visitors, invokes the method visitor class for each
 * method found.
 */
public class ClassVisitor extends EmptyVisitor {

    private JavaClass javaClass;
    private ConstantPoolGen constants;
    private String classReferenceFormat;
    private String pattern;
    public static Set<String> edgesMethods = new HashSet<String>();
    public static Set<String> edgesClass = new HashSet<String>();
    
    public ClassVisitor(JavaClass jClass,String pattern) {
        javaClass = jClass;
        constants = new ConstantPoolGen(javaClass.getConstantPool());
        classReferenceFormat = javaClass.getClassName() + " %s";
        this.pattern = pattern;
    }

    public void visitJavaClass(JavaClass jClass) {
        jClass.getConstantPool().accept(this);
        Method[] methods = jClass.getMethods();
        for (int i = 0; i < methods.length; i++)
            methods[i].accept(this);
    }

    public void visitConstantPool(ConstantPool constantPool) {
        for (int i = 0; i < constantPool.getLength(); i++) {
            Constant constant = constantPool.getConstant(i);
            if (constant == null)
                continue;
            if (constant.getTag() == 7 ) {
                String referencedClass = 
                    constantPool.constantToString(constant);
                
                if(referencedClass.contains(pattern)){
                	String output = String.format(classReferenceFormat,
                			referencedClass).replaceAll("[$\\d]+", "");
                	output = output.replaceAll("\\[L", "");
                	output = output.replaceAll("<init>", "").replaceAll("<cinit>", "");
                	ClassVisitor.edgesClass.add(output);
                	
                }
            }
        }
    }

    public void visitMethod(Method method) {
        MethodGen mg = new MethodGen(method, javaClass.getClassName(), constants);
        MethodVisitor visitor = new MethodVisitor(mg, javaClass, this.pattern);
        visitor.start(); 
    }

    public void start() {
        visitJavaClass(javaClass);
    }
}
