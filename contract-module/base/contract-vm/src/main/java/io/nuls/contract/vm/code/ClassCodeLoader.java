package io.nuls.contract.vm.code;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import io.nuls.contract.vm.util.Constants;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

import javax.annotation.Nonnull;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

public class ClassCodeLoader {

    private static final Map<String, ClassCode> RESOURCE_CLASS_CODES;

    private static final LoadingCache<ClassCodeCacheKey, Map<String, ClassCode>> CACHE;

    static {
        CACHE = CacheBuilder.newBuilder()
                .initialCapacity(1024)
                .maximumSize(10240)
                .expireAfterAccess(10 * 60, TimeUnit.SECONDS)
                .build(new CacheLoader<ClassCodeCacheKey, Map<String, ClassCode>>() {
                    @Override
                    public Map<String, ClassCode> load(@Nonnull final ClassCodeCacheKey cacheKey) {
                        return ClassCodeLoader.loadJar(cacheKey.getBytes());
                    }
                });
        RESOURCE_CLASS_CODES = loadFromResource();
    }

    public static void init() {

    }

    public static ClassCode load(String className) {
        try {
            ClassReader classReader = new ClassReader(className);
            return load(classReader);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static ClassCode loadFromResource(String className) {
        ClassCode classCode = RESOURCE_CLASS_CODES.get(className);
        if (classCode == null) {
            throw new RuntimeException("can't load class " + className);
        } else {
            return classCode;
        }
    }

    public static ClassCode getFromResource(String className) {
        return RESOURCE_CLASS_CODES.get(className);
    }

    public static ClassCode loadFromResourceOrTmp(String className) {
        ClassCode classCode = RESOURCE_CLASS_CODES.get(className);
        if (classCode == null) {
            try {
                File file = new File("/tmp/classes/" + className + ".class");
                if (file.exists()) {
                    byte[] bytes = FileUtils.readFileToByteArray(file);
                    return load(bytes);
                } else {
                    throw new RuntimeException("can't load class " + className);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            return classCode;
        }
    }

    public static void load(Map<String, ClassCode> classCodes, String className, Function<String, ClassCode> loader) {
        if (!classCodes.containsKey(className)) {
            ClassCode classCode = loader.apply(className);
            classCodes.put(className, classCode);
            if (StringUtils.isNotEmpty(classCode.superName)) {
                load(classCodes, classCode.superName, loader);
            }
            for (String interfaceName : classCode.interfaces) {
                load(classCodes, interfaceName, loader);
            }
            for (MethodCode methodCode : classCode.methods) {
                if (isSupport(methodCode.returnVariableType)) {
                    load(classCodes, methodCode.returnVariableType.getType(), loader);
                }
                for (VariableType variableType : methodCode.argsVariableType) {
                    if (isSupport(variableType)) {
                        load(classCodes, variableType.getType(), loader);
                    }
                }
            }
        }
    }

    public static Map<String, ClassCode> loadAll(String className, Function<String, ClassCode> loader) {
        Map<String, ClassCode> classCodes = new LinkedHashMap<>(1024);
        load(classCodes, className, loader);
        return classCodes;
    }

    public static Map<String, ClassCode> loadJarCache(byte[] bytes) {
        try {
            return CACHE.get(new ClassCodeCacheKey(bytes));
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    private static boolean isSupport(VariableType variableType) {
        if (variableType.isPrimitiveType()) {
            return false;
        } else if (variableType.isVoid()) {
            return false;
        } else {
            return true;
        }
    }

    private static ClassCode load(byte[] bytes) {
        return load(new ClassReader(bytes));
    }

    private static ClassCode load(ClassReader classReader) {
        ClassNode classNode = new ClassNode();
        classReader.accept(classNode, 0);
        ClassCode classCode = new ClassCode(classNode);
        return classCode;
    }

    private static Map<String, ClassCode> loadFromResource() {
        Map<String, ClassCode> map = new HashMap<>(1024);
        InputStream inputStream = ClassCodeLoader.class.getResourceAsStream("/used_classes");
        if (inputStream == null) {
            return map;
        } else {
            return loadJar(inputStream);
        }
    }

    private static Map<String, ClassCode> loadJar(byte[] bytes) {
        InputStream inputStream = new ByteArrayInputStream(bytes);
        return loadJar(inputStream);
    }

    private static Map<String, ClassCode> loadJar(InputStream inputStream) {
        try {
            JarInputStream jarInputStream = new JarInputStream(inputStream);
            return loadJar(jarInputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static Map<String, ClassCode> loadJar(JarInputStream jarInputStream) {
        Map<String, ClassCode> map = new HashMap<>(1024);
        try {
            JarEntry jarEntry;
            while ((jarEntry = jarInputStream.getNextJarEntry()) != null) {
                if (!jarEntry.isDirectory() && jarEntry.getName().endsWith(Constants.CLASS_SUFFIX)) {
                    byte[] bytes = IOUtils.toByteArray(jarInputStream);
                    ClassCode classCode = load(bytes);
                    map.put(classCode.name, classCode);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return map;
    }

}
