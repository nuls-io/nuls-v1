package io.nuls.task;

import io.nuls.exception.InchainException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Niels on 2017/9/26.
 * nuls.io
 */
@Service
public class ModuleManager implements ApplicationContextInitializer {

    private ApplicationContext applicationContext;

    private static final Map<String, InchainThread> threadMap = new HashMap<>();

    public Map<String, InchainModule> getModules() {
        Map<String, InchainModule> moduleMap = this.applicationContext.getBeansOfType(InchainModule.class);
        if (moduleMap == null || moduleMap.isEmpty()) {
            return moduleMap;
        }
        Map<String, InchainModule> resultMap = new HashMap<>();
        for (InchainModule module : moduleMap.values()) {
            resultMap.put(module.getModuleName(), module);
        }
        return resultMap;
    }

    public InchainModule getModule(String moduleName) {
        Map<String, InchainModule> moduleMap = this.applicationContext.getBeansOfType(InchainModule.class);
        if (moduleMap == null || moduleMap.isEmpty()) {
            return null;
        }
        InchainModule module = null;
        for (InchainModule mdl : moduleMap.values()) {
            if (mdl.getModuleName().equals(moduleName)) {
                module = mdl;
                break;
            }
        }
        return module;
    }

    public String getInfo() {
        Map<String, InchainModule> moduleMap = this.applicationContext.getBeansOfType(InchainModule.class);
        if (moduleMap == null || moduleMap.isEmpty()) {
            return "";
        }
        StringBuilder str = new StringBuilder();
        for (InchainModule module : moduleMap.values()) {
            str.append(module.getInfo());
        }
        return str.toString();
    }

    public static void regThread(String threadName, InchainThread thread) {
        if (threadMap.keySet().contains(threadName)) {
            throw new InchainException("the name of thread is already exist(" + threadName + ")");
        }
        threadMap.put(threadName, thread);
    }

    public InchainThread getThread(String threadName){
        return threadMap.get(threadName);
    }

    public Map<String, InchainThread> getAllThreads(){
        return threadMap;
    }

    public Map<String, InchainThread> getThreadsByModule(String moduleName){
        Map<String, InchainThread> map = new HashMap<>();
        for(InchainThread t:threadMap.values()){
            if(t.getModule().getModuleName().equals(moduleName)){
                map.put(t.getName(),t);
            }
        }
        return map;
    }

    @Override
    public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
        this.applicationContext = configurableApplicationContext;
    }
}
