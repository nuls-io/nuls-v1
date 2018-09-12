package io.nuls.contract.vm.natives.io.nuls.contract.sdk;

import io.nuls.contract.vm.Frame;
import io.nuls.contract.vm.MethodArgs;
import io.nuls.contract.vm.ObjectRef;
import io.nuls.contract.vm.Result;
import io.nuls.contract.vm.code.ClassCode;
import io.nuls.contract.vm.code.FieldCode;
import io.nuls.contract.vm.code.MethodCode;
import io.nuls.contract.vm.code.VariableType;
import io.nuls.contract.vm.exception.ErrorException;
import io.nuls.contract.vm.natives.NativeMethod;
import io.nuls.contract.vm.util.JsonUtils;
import org.spongycastle.util.encoders.Hex;

import java.util.LinkedHashMap;
import java.util.Map;

public class NativeUtils {

    public static final String TYPE = "io/nuls/contract/sdk/Utils";

    public static Result run(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        Result result = null;
        switch (methodCode.name) {
            case "revert":
                result = revert(methodCode, methodArgs, frame);
                break;
            case "emit":
                result = emit(methodCode, methodArgs, frame);
                break;
            case "encodeHexString":
                result = encodeHexString(methodCode, methodArgs, frame);
                break;
            case "decodeHex":
                result = decodeHex(methodCode, methodArgs, frame);
                break;
            default:
                frame.nonsupportMethod(methodCode);
                break;
        }
        return result;
    }

    private static Result revert(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        ObjectRef objectRef = (ObjectRef) methodArgs.invokeArgs[0];
        String errorMessage = null;
        if (objectRef != null) {
            errorMessage = frame.heap.runToString(objectRef);
        }
        throw new ErrorException(errorMessage, frame.vm.getGasUsed(), null);
    }

    private static Result emit(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        ObjectRef objectRef = (ObjectRef) methodArgs.invokeArgs[0];
        //String str = frame.heap.runToString(objectRef);
        ClassCode classCode = frame.methodArea.loadClass(objectRef.getVariableType().getType());
        Map<String, Object> jsonMap = toJson(objectRef, frame);
        EventJson eventJson = new EventJson();
        eventJson.setEvent(classCode.simpleName);
        eventJson.setPayload(jsonMap);
        String json = JsonUtils.toJson(eventJson);
        frame.vm.getEvents().add(json);
        Result result = NativeMethod.result(methodCode, null, frame);
        return result;
    }

    private static Map<String, Object> toJson(ObjectRef objectRef, Frame frame) {
        if (objectRef == null) {
            return null;
        }

        ClassCode classCode = frame.methodArea.loadClass(objectRef.getVariableType().getType());
        Map<String, Object> map = frame.heap.getFields(objectRef);
        Map<String, Object> jsonMap = new LinkedHashMap<>();
        for (String name : map.keySet()) {
            FieldCode fieldCode = classCode.fields.get(name);
            if (fieldCode != null && !fieldCode.isSynthetic) {
                Object value = map.get(name);
                jsonMap.put(name, toJson(fieldCode, value, frame));
            }
        }
        return jsonMap;
    }

    private static Object toJson(FieldCode fieldCode, Object value, Frame frame) {
        VariableType variableType = fieldCode.variableType;
        if (value == null) {
            return null;
        } else if (variableType.isPrimitive()) {
            return variableType.getPrimitiveValue(value);
        } else if (variableType.isArray()) {
            ObjectRef ref = (ObjectRef) value;
            if (variableType.isPrimitiveType() && variableType.getDimensions() == 1) {
                return frame.heap.getObject(ref);
            } else {
                int length = ref.getDimensions()[0];
                Object[] array = new Object[length];
                for (int i = 0; i < length; i++) {
                    Object item = frame.heap.getArray(ref, i);
                    if (item != null) {
                        ObjectRef itemRef = (ObjectRef) item;
                        item = frame.heap.runToString(itemRef);
                    }
                    array[i] = item;
                }
                return array;
            }
        } else {
            ObjectRef ref = (ObjectRef) value;
            return frame.heap.runToString(ref);
        }
    }

    private static Result encodeHexString(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        ObjectRef objectRef = (ObjectRef) methodArgs.invokeArgs[0];
        byte[] bytes = (byte[]) frame.heap.getObject(objectRef);
        String str = Hex.toHexString(bytes);
        ObjectRef strRef = frame.heap.newString(str);
        Result result = NativeMethod.result(methodCode, strRef, frame);
        return result;
    }

    private static Result decodeHex(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        ObjectRef objectRef = (ObjectRef) methodArgs.invokeArgs[0];
        String data = frame.heap.runToString(objectRef);
        byte[] bytes = Hex.decode(data);
        ObjectRef ref = frame.heap.newArray(bytes);
        Result result = NativeMethod.result(methodCode, ref, frame);
        return result;
    }

    static class EventJson {

        private String event;

        private Map<String, Object> payload;

        public String getEvent() {
            return event;
        }

        public void setEvent(String event) {
            this.event = event;
        }

        public Map<String, Object> getPayload() {
            return payload;
        }

        public void setPayload(Map<String, Object> payload) {
            this.payload = payload;
        }

    }

}
