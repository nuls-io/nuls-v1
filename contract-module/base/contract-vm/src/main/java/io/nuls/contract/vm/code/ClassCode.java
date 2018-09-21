/*
 * MIT License
 *
 * Copyright (c) 2017-2018 nuls.io
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */
package io.nuls.contract.vm.code;

import io.nuls.contract.vm.util.Constants;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.*;

import static io.nuls.contract.vm.util.Utils.arrayListInitialCapacity;
import static io.nuls.contract.vm.util.Utils.hashMapInitialCapacity;

public class ClassCode {

    /**
     * The class version. The minor version is stored in the 16 most significant bits, and the major
     * version in the 16 least significant bits.
     */
    public final int version;

    /**
     * The class's access flags (see {@link org.objectweb.asm.Opcodes}). This field also indicates if
     * the class is deprecated.
     */
    public final int access;

    /**
     * The internal name of this class (see {@link org.objectweb.asm.Type#getInternalName}).
     */
    public final String name;

    /**
     * The signature of this class. May be <tt>null</tt>.
     */
    public final String signature;

    /**
     * The internal of name of the super class (see {@link org.objectweb.asm.Type#getInternalName}).
     * For interfaces, the super class is {@link Object}. May be <tt>null</tt>, but only for the
     * {@link Object} class.
     */
    public final String superName;

    /**
     * The internal names of the interfaces directly implemented by this class (see {@link
     * org.objectweb.asm.Type#getInternalName}).
     */
    public final List<String> interfaces;

    /**
     * The name of the source file from which this class was compiled. May be <tt>null</tt>.
     */
    public final String sourceFile;

    /**
     * The correspondence between source and compiled elements of this class. May be <tt>null</tt>.
     */
    public final String sourceDebug;

    /**
     * The module stored in this class. May be <tt>null</tt>.
     */
    public final ModuleNode module;

    /**
     * The internal name of the enclosing class of this class. May be <tt>null</tt>.
     */
    public final String outerClass;

    /**
     * The name of the method that contains this class, or <tt>null</tt> if this class is not enclosed
     * in a method.
     */
    public final String outerMethod;

    /**
     * The descriptor of the method that contains this class, or <tt>null</tt> if this class is not
     * enclosed in a method.
     */
    public final String outerMethodDesc;

    /**
     * The runtime visible annotations of this class. May be <tt>null</tt>.
     */
    public final List<AnnotationNode> visibleAnnotations;

    /**
     * The runtime invisible annotations of this class. May be <tt>null</tt>.
     */
    public final List<AnnotationNode> invisibleAnnotations;

    /**
     * The runtime visible type annotations of this class. May be <tt>null</tt>.
     */
    public final List<TypeAnnotationNode> visibleTypeAnnotations;

    /**
     * The runtime invisible type annotations of this class. May be <tt>null</tt>.
     */
    public final List<TypeAnnotationNode> invisibleTypeAnnotations;

    /**
     * The non standard attributes of this class. May be <tt>null</tt>.
     */
    public final List<Attribute> attrs;

    /**
     * The inner classes of this class.
     */
    public final List<InnerClassNode> innerClasses;

    /**
     * <b>Experimental, use at your own risk. This field will be renamed when it becomes stable, this
     * will break existing code using it</b>. The internal name of the nest host class of this class.
     * May be <tt>null</tt>.
     */
    public final String nestHostClassExperimental;

    /**
     * <b>Experimental, use at your own risk. This field will be renamed when it becomes stable, this
     * will break existing code using it</b>. The internal names of the nest members of this class.
     * May be <tt>null</tt>.
     */
    public final List<String> nestMembersExperimental;

    /**
     * The fields of this class.
     */
    //public final List<FieldNode> fields;
    public final Map<String, FieldCode> fields;

    /**
     * The methods of this class.
     */
    //public final List<MethodNode> methods;
    public final List<MethodCode> methods;
    private final Map<String, MethodCode> methodMap;

    public final VariableType variableType;

    public final boolean isInterface;

    public final boolean isSuper;

    public final boolean isAbstract;

    public final boolean isV1_6;

    public final boolean isV1_8;

    public final String simpleName;

    public ClassCode(ClassNode classNode) {
        version = classNode.version;
        access = classNode.access;
        name = classNode.name;
        signature = classNode.signature;
        superName = classNode.superName;
        interfaces = ListUtils.emptyIfNull(classNode.interfaces);
        sourceFile = classNode.sourceFile;
        sourceDebug = classNode.sourceDebug;
        module = classNode.module;
        outerClass = classNode.outerClass;
        outerMethod = classNode.outerMethod;
        outerMethodDesc = classNode.outerMethodDesc;
        visibleAnnotations = ListUtils.emptyIfNull(classNode.visibleAnnotations);
        invisibleAnnotations = ListUtils.emptyIfNull(classNode.invisibleAnnotations);
        visibleTypeAnnotations = ListUtils.emptyIfNull(classNode.visibleTypeAnnotations);
        invisibleTypeAnnotations = ListUtils.emptyIfNull(classNode.invisibleTypeAnnotations);
        attrs = ListUtils.emptyIfNull(classNode.attrs);
        innerClasses = ListUtils.emptyIfNull(classNode.innerClasses);
        nestHostClassExperimental = classNode.nestHostClassExperimental;
        nestMembersExperimental = ListUtils.emptyIfNull(classNode.nestMembersExperimental);
        //fields = ListUtils.emptyIfNull(classNode.fields);
        //methods = ListUtils.emptyIfNull(classNode.methods);
        final List<FieldNode> fieldNodes = ListUtils.emptyIfNull(classNode.fields);
        fields = new LinkedHashMap<>(hashMapInitialCapacity(fieldNodes.size()));
        for (FieldNode fieldNode : fieldNodes) {
            final FieldCode fieldCode = new FieldCode(fieldNode);
            fields.put(fieldCode.name, fieldCode);
        }
        final List<MethodNode> methodNodes = ListUtils.emptyIfNull(classNode.methods);
        methods = new ArrayList<>(arrayListInitialCapacity(methodNodes.size()));
        methodMap = new HashMap<>(hashMapInitialCapacity(methodNodes.size() * 2));
        for (MethodNode methodNode : methodNodes) {
            final MethodCode methodCode = new MethodCode(this, methodNode);
            methods.add(methodCode);
            methodMap.put(methodCode.nameDesc, methodCode);
            if (!methodMap.containsKey(methodCode.name)) {
                methodMap.put(methodCode.name, methodCode);
            }
        }
        variableType = VariableType.valueOf(name);
        isInterface = (access & Opcodes.ACC_INTERFACE) != 0;
        isSuper = (access & Opcodes.ACC_SUPER) != 0;
        isAbstract = (access & Opcodes.ACC_ABSTRACT) != 0;
        isV1_6 = (version & Opcodes.V1_6) != 0;
        isV1_8 = (version & Opcodes.V1_8) != 0;
        simpleName = getSimpleName();
    }

    public MethodCode getMethodCode(String methodName, String methodDesc) {
        if (StringUtils.isEmpty(methodDesc)) {
            return this.methodMap.get(methodName);
        } else {
            return this.methodMap.get(methodName + methodDesc);
        }
    }

    private String getSimpleName() {
        int i = this.name.lastIndexOf(Constants.DOLLAR);
        if (i > 0) {
            return this.name.substring(i + 1);
        } else {
            i = this.name.lastIndexOf(Constants.CLASS_SEPARATOR);
            if (i > 0) {
                return this.name.substring(i + 1);
            } else {
                return this.name;
            }
        }
    }

    public boolean isSyntheticField(String fieldName) {
        FieldCode fieldCode = fields.get(fieldName);
        return fieldCode != null && fieldCode.isSynthetic;
    }

}
