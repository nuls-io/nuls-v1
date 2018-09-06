package io.nuls.contract.vm.code;

import org.apache.commons.lang3.StringUtils;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.InnerClassNode;
import org.objectweb.asm.tree.ModuleNode;
import org.objectweb.asm.tree.TypeAnnotationNode;

import java.util.List;
import java.util.Objects;

public class ClassCode {

    private int version;

    private int access;

    private String name;

    private String signature;

    private String superName;

    private List<String> interfaces;

    private String sourceFile;

    private String sourceDebug;

    private ModuleNode module;

    private String outerClass;

    private String outerMethod;

    private String outerMethodDesc;

    private List<AnnotationNode> visibleAnnotations;

    private List<AnnotationNode> invisibleAnnotations;

    private List<TypeAnnotationNode> visibleTypeAnnotations;

    private List<TypeAnnotationNode> invisibleTypeAnnotations;

    private List<Attribute> attrs;

    private List<InnerClassNode> innerClasses;

    private List<FieldCode> fields;

    private List<MethodCode> methods;

    private VariableType variableType;

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

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

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getSuperName() {
        return superName;
    }

    public void setSuperName(String superName) {
        this.superName = superName;
    }

    public List<String> getInterfaces() {
        return interfaces;
    }

    public void setInterfaces(List<String> interfaces) {
        this.interfaces = interfaces;
    }

    public String getSourceFile() {
        return sourceFile;
    }

    public void setSourceFile(String sourceFile) {
        this.sourceFile = sourceFile;
    }

    public String getSourceDebug() {
        return sourceDebug;
    }

    public void setSourceDebug(String sourceDebug) {
        this.sourceDebug = sourceDebug;
    }

    public ModuleNode getModule() {
        return module;
    }

    public void setModule(ModuleNode module) {
        this.module = module;
    }

    public String getOuterClass() {
        return outerClass;
    }

    public void setOuterClass(String outerClass) {
        this.outerClass = outerClass;
    }

    public String getOuterMethod() {
        return outerMethod;
    }

    public void setOuterMethod(String outerMethod) {
        this.outerMethod = outerMethod;
    }

    public String getOuterMethodDesc() {
        return outerMethodDesc;
    }

    public void setOuterMethodDesc(String outerMethodDesc) {
        this.outerMethodDesc = outerMethodDesc;
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

    public List<InnerClassNode> getInnerClasses() {
        return innerClasses;
    }

    public void setInnerClasses(List<InnerClassNode> innerClasses) {
        this.innerClasses = innerClasses;
    }

    public List<FieldCode> getFields() {
        return fields;
    }

    public void setFields(List<FieldCode> fields) {
        this.fields = fields;
    }

    public List<MethodCode> getMethods() {
        return methods;
    }

    public void setMethods(List<MethodCode> methods) {
        this.methods = methods;
    }

    public VariableType getVariableType() {
        return variableType;
    }

    public void setVariableType(VariableType variableType) {
        this.variableType = variableType;
    }

    public MethodCode getMethodCode(String methodName, String methodDesc) {
        if (StringUtils.isEmpty(methodDesc)) {
            return getMethodCode(methodName);
        }
        return this.methods.stream().filter(methodCode ->
                Objects.equals(methodCode.getName(), methodName) &&
                        Objects.equals(methodCode.getDesc(), methodDesc)
        ).findFirst().orElse(null);
    }

    public MethodCode getMethodCode(String methodName) {
        return this.methods.stream().filter(methodCode ->
                Objects.equals(methodCode.getName(), methodName)
        ).findFirst().orElse(null);
    }

    public boolean isInterface() {
        return (access & Opcodes.ACC_INTERFACE) != 0;
    }

    public boolean isSuper() {
        return (access & Opcodes.ACC_SUPER) != 0;
    }

    public boolean isAbstract() {
        return (access & Opcodes.ACC_ABSTRACT) != 0;
    }

    public boolean isNotAbstract() {
        return !isAbstract();
    }

    public boolean isV1_6() {
        return (version & Opcodes.V1_6) != 0;
    }

    public boolean isV1_7() {
        return (version & Opcodes.V1_7) != 0;
    }

    public boolean isV1_8() {
        return (version & Opcodes.V1_8) != 0;
    }

    public String getSimpleName() {
        int i = this.name.lastIndexOf("$");
        if (i > 0) {
            return this.name.substring(i + 1);
        }
        i = this.name.lastIndexOf("/");
        if (i > 0) {
            return this.name.substring(i + 1);
        }
        return this.name;
    }

    public boolean isSynthetic(String fieldName) {
        return fields.stream()
                .filter(FieldCode::isSynthetic)
                .filter(fieldCode -> fieldCode.getName().equals(fieldName))
                .count() > 0;
    }

    public FieldCode getFieldCode(String fieldName) {
        return fields.stream()
                .filter(fieldCode -> !fieldCode.isSynthetic())
                .filter(fieldCode -> fieldCode.getName().equals(fieldName))
                .findFirst()
                .orElse(null);
    }

}
