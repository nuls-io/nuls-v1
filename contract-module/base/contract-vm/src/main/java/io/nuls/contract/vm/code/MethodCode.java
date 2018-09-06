package io.nuls.contract.vm.code;

import io.nuls.contract.vm.program.ProgramMethodArg;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class MethodCode {

    private int access;

    private String name;

    private String desc;

    private String normalDesc;

    private String signature;

    private List<String> exceptions;

    private List<ParameterNode> parameters;

    private List<AnnotationNode> visibleAnnotations;

    private List<AnnotationNode> invisibleAnnotations;

    private List<TypeAnnotationNode> visibleTypeAnnotations;

    private List<TypeAnnotationNode> invisibleTypeAnnotations;

    private List<Attribute> attrs;

    private Object annotationDefault;

    private List<AnnotationNode>[] visibleParameterAnnotations;

    private List<AnnotationNode>[] invisibleParameterAnnotations;

    private InsnList instructions;

    private List<TryCatchBlockNode> tryCatchBlocks;

    private int maxStack;

    private int maxLocals;

    private List<LocalVariableCode> localVariables;

    private List<LocalVariableAnnotationNode> visibleLocalVariableAnnotations;

    private List<LocalVariableAnnotationNode> invisibleLocalVariableAnnotations;

    //private boolean visited;

    private ClassCode classCode;

    private VariableType returnVariableType;

    private String returnArg;

    private List<VariableType> argsVariableType;

    private List<ProgramMethodArg> args;

    public int getAccess() {
        return access;
    }

    public void setAccess(int access) {
        this.access = access;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getNormalDesc() {
        return normalDesc;
    }

    public void setNormalDesc(String normalDesc) {
        this.normalDesc = normalDesc;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public List<String> getExceptions() {
        return exceptions;
    }

    public void setExceptions(List<String> exceptions) {
        this.exceptions = exceptions;
    }

    public List<ParameterNode> getParameters() {
        return parameters;
    }

    public void setParameters(List<ParameterNode> parameters) {
        this.parameters = parameters;
    }

    public List<AnnotationNode> getVisibleAnnotations() {
        return visibleAnnotations;
    }

    public void setVisibleAnnotations(List<AnnotationNode> visibleAnnotations) {
        this.visibleAnnotations = visibleAnnotations;
    }

    public List<AnnotationNode> getInvisibleAnnotations() {
        return invisibleAnnotations;
    }

    public void setInvisibleAnnotations(List<AnnotationNode> invisibleAnnotations) {
        this.invisibleAnnotations = invisibleAnnotations;
    }

    public List<TypeAnnotationNode> getVisibleTypeAnnotations() {
        return visibleTypeAnnotations;
    }

    public void setVisibleTypeAnnotations(List<TypeAnnotationNode> visibleTypeAnnotations) {
        this.visibleTypeAnnotations = visibleTypeAnnotations;
    }

    public List<TypeAnnotationNode> getInvisibleTypeAnnotations() {
        return invisibleTypeAnnotations;
    }

    public void setInvisibleTypeAnnotations(List<TypeAnnotationNode> invisibleTypeAnnotations) {
        this.invisibleTypeAnnotations = invisibleTypeAnnotations;
    }

    public List<Attribute> getAttrs() {
        return attrs;
    }

    public void setAttrs(List<Attribute> attrs) {
        this.attrs = attrs;
    }

    public Object getAnnotationDefault() {
        return annotationDefault;
    }

    public void setAnnotationDefault(Object annotationDefault) {
        this.annotationDefault = annotationDefault;
    }

    public List<AnnotationNode>[] getVisibleParameterAnnotations() {
        return visibleParameterAnnotations;
    }

    public void setVisibleParameterAnnotations(List<AnnotationNode>[] visibleParameterAnnotations) {
        this.visibleParameterAnnotations = visibleParameterAnnotations;
    }

    public List<AnnotationNode>[] getInvisibleParameterAnnotations() {
        return invisibleParameterAnnotations;
    }

    public void setInvisibleParameterAnnotations(List<AnnotationNode>[] invisibleParameterAnnotations) {
        this.invisibleParameterAnnotations = invisibleParameterAnnotations;
    }

    public InsnList getInstructions() {
        return instructions;
    }

    public void setInstructions(InsnList instructions) {
        this.instructions = instructions;
    }

    public List<TryCatchBlockNode> getTryCatchBlocks() {
        return tryCatchBlocks;
    }

    public void setTryCatchBlocks(List<TryCatchBlockNode> tryCatchBlocks) {
        this.tryCatchBlocks = tryCatchBlocks;
    }

    public int getMaxStack() {
        return maxStack;
    }

    public void setMaxStack(int maxStack) {
        this.maxStack = maxStack;
    }

    public int getMaxLocals() {
        return maxLocals;
    }

    public void setMaxLocals(int maxLocals) {
        this.maxLocals = maxLocals;
    }

    public List<LocalVariableCode> getLocalVariables() {
        return localVariables;
    }

    public void setLocalVariables(List<LocalVariableCode> localVariables) {
        this.localVariables = localVariables;
    }

    public List<LocalVariableAnnotationNode> getVisibleLocalVariableAnnotations() {
        return visibleLocalVariableAnnotations;
    }

    public void setVisibleLocalVariableAnnotations(List<LocalVariableAnnotationNode> visibleLocalVariableAnnotations) {
        this.visibleLocalVariableAnnotations = visibleLocalVariableAnnotations;
    }

    public List<LocalVariableAnnotationNode> getInvisibleLocalVariableAnnotations() {
        return invisibleLocalVariableAnnotations;
    }

    public void setInvisibleLocalVariableAnnotations(List<LocalVariableAnnotationNode> invisibleLocalVariableAnnotations) {
        this.invisibleLocalVariableAnnotations = invisibleLocalVariableAnnotations;
    }

    public ClassCode getClassCode() {
        return classCode;
    }

    public void setClassCode(ClassCode classCode) {
        this.classCode = classCode;
    }

    public VariableType getReturnVariableType() {
        return returnVariableType;
    }

    public void setReturnVariableType(VariableType returnVariableType) {
        this.returnVariableType = returnVariableType;
    }

    public String getReturnArg() {
        return returnArg;
    }

    public void setReturnArg(String returnArg) {
        this.returnArg = returnArg;
    }

    public List<VariableType> getArgsVariableType() {
        return argsVariableType;
    }

    public void setArgsVariableType(List<VariableType> argsVariableType) {
        this.argsVariableType = argsVariableType;
    }

    public List<ProgramMethodArg> getArgs() {
        return args;
    }

    public void setArgs(List<ProgramMethodArg> args) {
        this.args = args;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MethodCode that = (MethodCode) o;

        if (access != that.access) return false;
        if (maxStack != that.maxStack) return false;
        if (maxLocals != that.maxLocals) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (desc != null ? !desc.equals(that.desc) : that.desc != null) return false;
        if (normalDesc != null ? !normalDesc.equals(that.normalDesc) : that.normalDesc != null) return false;
        if (signature != null ? !signature.equals(that.signature) : that.signature != null) return false;
        if (exceptions != null ? !exceptions.equals(that.exceptions) : that.exceptions != null) return false;
        if (parameters != null ? !parameters.equals(that.parameters) : that.parameters != null) return false;
        if (visibleAnnotations != null ? !visibleAnnotations.equals(that.visibleAnnotations) : that.visibleAnnotations != null)
            return false;
        if (invisibleAnnotations != null ? !invisibleAnnotations.equals(that.invisibleAnnotations) : that.invisibleAnnotations != null)
            return false;
        if (visibleTypeAnnotations != null ? !visibleTypeAnnotations.equals(that.visibleTypeAnnotations) : that.visibleTypeAnnotations != null)
            return false;
        if (invisibleTypeAnnotations != null ? !invisibleTypeAnnotations.equals(that.invisibleTypeAnnotations) : that.invisibleTypeAnnotations != null)
            return false;
        if (attrs != null ? !attrs.equals(that.attrs) : that.attrs != null) return false;
        if (annotationDefault != null ? !annotationDefault.equals(that.annotationDefault) : that.annotationDefault != null)
            return false;
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        if (!Arrays.equals(visibleParameterAnnotations, that.visibleParameterAnnotations)) return false;
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        if (!Arrays.equals(invisibleParameterAnnotations, that.invisibleParameterAnnotations)) return false;
        if (instructions != null ? !instructions.equals(that.instructions) : that.instructions != null) return false;
        if (tryCatchBlocks != null ? !tryCatchBlocks.equals(that.tryCatchBlocks) : that.tryCatchBlocks != null)
            return false;
        if (localVariables != null ? !localVariables.equals(that.localVariables) : that.localVariables != null)
            return false;
        if (visibleLocalVariableAnnotations != null ? !visibleLocalVariableAnnotations.equals(that.visibleLocalVariableAnnotations) : that.visibleLocalVariableAnnotations != null)
            return false;
        if (invisibleLocalVariableAnnotations != null ? !invisibleLocalVariableAnnotations.equals(that.invisibleLocalVariableAnnotations) : that.invisibleLocalVariableAnnotations != null)
            return false;
        if (classCode != null ? !classCode.equals(that.classCode) : that.classCode != null) return false;
        if (returnVariableType != null ? !returnVariableType.equals(that.returnVariableType) : that.returnVariableType != null)
            return false;
        if (returnArg != null ? !returnArg.equals(that.returnArg) : that.returnArg != null) return false;
        if (argsVariableType != null ? !argsVariableType.equals(that.argsVariableType) : that.argsVariableType != null)
            return false;
        return args != null ? args.equals(that.args) : that.args == null;
    }

    @Override
    public int hashCode() {
        int result = access;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (desc != null ? desc.hashCode() : 0);
        result = 31 * result + (normalDesc != null ? normalDesc.hashCode() : 0);
        result = 31 * result + (signature != null ? signature.hashCode() : 0);
        result = 31 * result + (exceptions != null ? exceptions.hashCode() : 0);
        result = 31 * result + (parameters != null ? parameters.hashCode() : 0);
        result = 31 * result + (visibleAnnotations != null ? visibleAnnotations.hashCode() : 0);
        result = 31 * result + (invisibleAnnotations != null ? invisibleAnnotations.hashCode() : 0);
        result = 31 * result + (visibleTypeAnnotations != null ? visibleTypeAnnotations.hashCode() : 0);
        result = 31 * result + (invisibleTypeAnnotations != null ? invisibleTypeAnnotations.hashCode() : 0);
        result = 31 * result + (attrs != null ? attrs.hashCode() : 0);
        result = 31 * result + (annotationDefault != null ? annotationDefault.hashCode() : 0);
        result = 31 * result + Arrays.hashCode(visibleParameterAnnotations);
        result = 31 * result + Arrays.hashCode(invisibleParameterAnnotations);
        result = 31 * result + (instructions != null ? instructions.hashCode() : 0);
        result = 31 * result + (tryCatchBlocks != null ? tryCatchBlocks.hashCode() : 0);
        result = 31 * result + maxStack;
        result = 31 * result + maxLocals;
        result = 31 * result + (localVariables != null ? localVariables.hashCode() : 0);
        result = 31 * result + (visibleLocalVariableAnnotations != null ? visibleLocalVariableAnnotations.hashCode() : 0);
        result = 31 * result + (invisibleLocalVariableAnnotations != null ? invisibleLocalVariableAnnotations.hashCode() : 0);
        result = 31 * result + (classCode != null ? classCode.hashCode() : 0);
        result = 31 * result + (returnVariableType != null ? returnVariableType.hashCode() : 0);
        result = 31 * result + (returnArg != null ? returnArg.hashCode() : 0);
        result = 31 * result + (argsVariableType != null ? argsVariableType.hashCode() : 0);
        result = 31 * result + (args != null ? args.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "MethodCode{" +
                "access=" + access +
                ", name='" + name + '\'' +
                ", desc='" + desc + '\'' +
                ", normalDesc='" + normalDesc + '\'' +
                ", signature='" + signature + '\'' +
                ", exceptions=" + exceptions +
                ", parameters=" + parameters +
                ", visibleAnnotations=" + visibleAnnotations +
                ", invisibleAnnotations=" + invisibleAnnotations +
                ", visibleTypeAnnotations=" + visibleTypeAnnotations +
                ", invisibleTypeAnnotations=" + invisibleTypeAnnotations +
                ", attrs=" + attrs +
                ", annotationDefault=" + annotationDefault +
                ", visibleParameterAnnotations=" + Arrays.toString(visibleParameterAnnotations) +
                ", invisibleParameterAnnotations=" + Arrays.toString(invisibleParameterAnnotations) +
                ", instructions=" + instructions +
                ", tryCatchBlocks=" + tryCatchBlocks +
                ", maxStack=" + maxStack +
                ", maxLocals=" + maxLocals +
                ", localVariables=" + localVariables +
                ", visibleLocalVariableAnnotations=" + visibleLocalVariableAnnotations +
                ", invisibleLocalVariableAnnotations=" + invisibleLocalVariableAnnotations +
                ", classCode=" + classCode +
                ", returnVariableType=" + returnVariableType +
                ", returnArg='" + returnArg + '\'' +
                ", argsVariableType=" + argsVariableType +
                ", args=" + args +
                '}';
    }

    public boolean isPublic() {
        return (access & Opcodes.ACC_PUBLIC) != 0;
    }

    public boolean isStatic() {
        return (access & Opcodes.ACC_STATIC) != 0;
    }

    public boolean isAbstract() {
        return (access & Opcodes.ACC_ABSTRACT) != 0;
    }

    public boolean isNative() {
        return (access & Opcodes.ACC_NATIVE) != 0;
    }

    public boolean isNotAbstract() {
        return !isAbstract();
    }

    public boolean isConstructor() {
        return "<init>".equals(this.name);
    }

    public boolean hasViewAnnotation() {
        return hasAnnotation("Lio/nuls/contract/sdk/annotation/View;");
    }

    public boolean hasPayableAnnotation() {
        return hasAnnotation("Lio/nuls/contract/sdk/annotation/Payable;");
    }

    public boolean hasAnnotation(String annotation) {
        boolean isView = this.visibleAnnotations.stream()
                .filter(annotationNode -> annotation.equals(annotationNode.desc))
                .count() >= 1;
        return isView;
    }

    public boolean hasRequiredAnnotation(int i) {
        List<AnnotationNode>[] annotations = getVisibleParameterAnnotations();
        if (annotations == null || annotations.length < 1 || annotations.length <= i) {
            return false;
        }
        List<AnnotationNode> list = annotations[i];
        if (list == null) {
            return false;
        }
        boolean b = list.stream()
                .filter(annotationNode -> "Lio/nuls/contract/sdk/annotation/Required;".equals(annotationNode.desc))
                .count() >= 1;
        return b;
    }

    public LocalVariableCode getLocalVariableCode(int index) {
        return getLocalVariables().stream().filter(localVariableCode -> localVariableCode.index == index)
                .findFirst().orElse(null);
    }

    public boolean isClass(String className) {
        return Objects.equals(this.classCode.getName(), className);
    }

    public boolean isMethod(String name, String desc) {
        return Objects.equals(this.name, name) && Objects.equals(this.desc, desc);
    }

}
