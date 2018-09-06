package io.nuls.contract.vm.natives.io.nuls.contract.sdk;

import io.nuls.contract.vm.*;
import io.nuls.contract.vm.code.ClassCode;
import io.nuls.contract.vm.code.FieldCode;
import io.nuls.contract.vm.code.MethodCode;
import io.nuls.contract.vm.code.VariableType;
import io.nuls.contract.vm.natives.NativeMethod;
import io.nuls.contract.vm.util.JsonUtils;
import org.spongycastle.util.encoders.Hex;

import java.util.LinkedHashMap;
import java.util.Map;

public class NativeUtils {

    public static final String TYPE = "io/nuls/contract/sdk/Utils";

    public static Result run(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        Result result = null;
        switch (methodCode.getName()) {
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
        ObjectRef objectRef = (ObjectRef) methodArgs.getInvokeArgs()[0];
        String errorMessage = null;
        if (objectRef != null) {
            errorMessage = frame.getHeap().runToString(objectRef);
        }
        throw new RevertException(errorMessage, null);
    }

    private static Result emit(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        ObjectRef objectRef = (ObjectRef) methodArgs.getInvokeArgs()[0];
        //String str = frame.getHeap().runToString(objectRef);
        ClassCode classCode = frame.getMethodArea().loadClass(objectRef.getVariableType().getType());
        Map<String, Object> jsonMap = toJson(objectRef, frame);
        EventJson eventJson = new EventJson();
        eventJson.setEvent(classCode.getSimpleName());
        eventJson.setPayload(jsonMap);
        String json = JsonUtils.toJson(eventJson);
        frame.getVm().getEvents().add(json);
        Result result = NativeMethod.result(methodCode, null, frame);
        return result;
    }

    private static Map<String, Object> toJson(ObjectRef objectRef, Frame frame) {
        if (objectRef == null) {
            return null;
        }

        ClassCode classCode = frame.getMethodArea().loadClass(objectRef.getVariableType().getType());
        Map<String, Object> map = frame.getHeap().getFields(objectRef);
        Map<String, Object> jsonMap = new LinkedHashMap<>();
        for (String name : map.keySet()) {
            FieldCode fieldCode = classCode.getFieldCode(name);
            if (fieldCode != null) {
                Object value = map.get(name);
                jsonMap.put(name, toJson(fieldCode, value, frame));
            }
        }
        return jsonMap;
    }

    private static Object toJson(FieldCode fieldCode, Object value, Frame frame) {
        VariableType variableType = fieldCode.getVariableType();
        if (value == null) {
            return null;
        } else if (variableType.isPrimitive()) {
            return variableType.getPrimitiveValue(value);
        } else if (variableType.isArray()) {
            ObjectRef ref = (ObjectRef) value;
            if (variableType.isPrimitiveType() && variableType.getDimensions() == 1) {
                return frame.getHeap().getObject(ref);
            } else {
                int length = ref.getDimensions()[0];
                Object[] array = new Object[length];
                for (int i = 0; i < length; i++) {
                    Object item = frame.getHeap().getArray(ref, i);
                    if (item != null) {
                        ObjectRef itemRef = (ObjectRef) item;
                        item = frame.getHeap().runToString(itemRef);
                    }
                    array[i] = item;
                }
                return array;
            }
        } else {
            ObjectRef ref = (ObjectRef) value;
            return frame.getHeap().runToString(ref);
        }
    }

    private static Result encodeHexString(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        ObjectRef objectRef = (ObjectRef) methodArgs.getInvokeArgs()[0];
        byte[] bytes = (byte[]) frame.getHeap().getObject(objectRef);
        String str = Hex.toHexString(bytes);
        ObjectRef strRef = frame.getHeap().newString(str);
        Result result = NativeMethod.result(methodCode, strRef, frame);
        return result;
    }

    private static Result decodeHex(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        ObjectRef objectRef = (ObjectRef) methodArgs.getInvokeArgs()[0];
        String data = frame.getHeap().runToString(objectRef);
        byte[] bytes = Hex.decode(data);
        ObjectRef ref = frame.getHeap().newArray(bytes);
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
