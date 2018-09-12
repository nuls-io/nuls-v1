package io.nuls.contract.vm.code;

import org.apache.commons.collections4.ListUtils;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.TypeAnnotationNode;

import java.util.List;

public class FieldCode {

    /**
     * The field's access flags (see {@link org.objectweb.asm.Opcodes}). This field also indicates if
     * the field is synthetic and/or deprecated.
     */
    public final int access;

    /**
     * The field's name.
     */
    public final String name;

    /**
     * The field's descriptor (see {@link org.objectweb.asm.Type}).
     */
    public final String desc;

    /**
     * The field's signature. May be <tt>null</tt>.
     */
    public final String signature;

    /**
     * The field's initial value. This field, which may be <tt>null</tt> if the field does not have an
     * initial value, must be an {@link Integer}, a {@link Float}, a {@link Long}, a {@link Double} or
     * a {@link String}.
     */
    public final Object value;

    /**
     * The runtime visible annotations of this field. May be <tt>null</tt>.
     */
    public final List<AnnotationNode> visibleAnnotations;

    /**
     * The runtime invisible annotations of this field. May be <tt>null</tt>.
     */
    public final List<AnnotationNode> invisibleAnnotations;

    /**
     * The runtime visible type annotations of this field. May be <tt>null</tt>.
     */
    public final List<TypeAnnotationNode> visibleTypeAnnotations;

    /**
     * The runtime invisible type annotations of this field. May be <tt>null</tt>.
     */
    public final List<TypeAnnotationNode> invisibleTypeAnnotations;

    /**
     * The non standard attributes of this field. * May be <tt>null</tt>.
     */
    public final List<Attribute> attrs;

    public final VariableType variableType;

    public final boolean isStatic;

    public final boolean isFinal;

    public final boolean isSynthetic;

    public FieldCode(FieldNode fieldNode) {
        access = fieldNode.access;
        name = fieldNode.name;
        desc = fieldNode.desc;
        signature = fieldNode.signature;
        value = fieldNode.value;
        visibleAnnotations = ListUtils.emptyIfNull(fieldNode.visibleAnnotations);
        invisibleAnnotations = ListUtils.emptyIfNull(fieldNode.invisibleAnnotations);
        visibleTypeAnnotations = ListUtils.emptyIfNull(fieldNode.visibleTypeAnnotations);
        invisibleTypeAnnotations = ListUtils.emptyIfNull(fieldNode.invisibleTypeAnnotations);
        attrs = ListUtils.emptyIfNull(fieldNode.attrs);
        //
        variableType = VariableType.valueOf(desc);
        isStatic = (access & Opcodes.ACC_STATIC) != 0;
        isFinal = (access & Opcodes.ACC_FINAL) != 0;
        isSynthetic = (access & Opcodes.ACC_SYNTHETIC) != 0;
    }

}
