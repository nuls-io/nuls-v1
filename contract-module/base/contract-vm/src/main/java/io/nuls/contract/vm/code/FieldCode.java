package io.nuls.contract.vm.code;

import org.objectweb.asm.Attribute;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.TypeAnnotationNode;

import java.util.List;

public class FieldCode {

    private int access;

    private String name;

    private String desc;

    private String signature;

    private Object value;

    private List<AnnotationNode> visibleAnnotations;

    private List<AnnotationNode> invisibleAnnotations;

    private List<TypeAnnotationNode> visibleTypeAnnotations;

    private List<TypeAnnotationNode> invisibleTypeAnnotations;

    private List<Attribute> attrs;

    private VariableType variableType;

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

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
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

    public VariableType getVariableType() {
        return variableType;
    }

    public void setVariableType(VariableType variableType) {
        this.variableType = variableType;
    }

    public boolean isStatic() {
        return (access & Opcodes.ACC_STATIC) != 0;
    }

    public boolean isNotStatic() {
        return !isStatic();
    }

    public boolean isFinal() {
        return (access & Opcodes.ACC_FINAL) != 0;
    }

    public boolean isNotFinal() {
        return !isFinal();
    }

    public boolean isSynthetic() {
        return (access & Opcodes.ACC_SYNTHETIC) != 0;
    }

}
