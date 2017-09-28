package io.nuls.task;

import io.nuls.exception.NulsException;
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

    private static final Map<String, NulsThread> threadMap = new HashMap<>();

    public Map<String, NulsModule> getModules() {
        Map<String, NulsModule> moduleMap = this.applicationContext.getBeansOfType(NulsModule.class);
        if (moduleMap == null || moduleMap.isEmpty()) {
            return moduleMap;
        }
        Map<String, NulsModule> resultMap = new HashMap<>();
        for (NulsModule module : moduleMap.values()) {
            resultMap.put(module.getModuleName(), module);
        }
        return resultMap;
    }

    public NulsModule getModule(String moduleName) {
        Map<String, NulsModule> moduleMap = this.applicationContext.getBeansOfType(NulsModule.class);
        if (moduleMap == null || moduleMap.isEmpty()) {
            return null;
        }
        NulsModule module = null;
        for (NulsModule mdl : moduleMap.values()) {
            if (mdl.getModuleName().equals(moduleName)) {
                module = mdl;
                break;
            }
        }
        return module;
    }

    public String getInfo() {
        Map<String, NulsModule> moduleMap = this.applicationContext.getBeansOfType(NulsModule.class);
        if (moduleMap == null || moduleMap.isEmpty()) {
            return "";
        }
        StringBuilder str = new StringBuilder();
        for (NulsModule module : moduleMap.values()) {
            str.append(module.getInfo());
        }
        return str.toString();
    }

    public static void regThread(String threadName, NulsThread thread) {
        if (threadMap.keySet().contains(threadName)) {
            throw new NulsException("the name of thread is already exist(" + threadName + ")");
        }
        threadMap.put(threadName, thread);
    }

    public NulsThread getThread(String threadName){
        return threadMap.get(threadName);
    }

    public Map<String, NulsThread> getAllThreads(){
        return threadMap;
    }

    public Map<String, NulsThread> getThreadsByModule(String moduleName){
        Map<String, NulsThread> map = new HashMap<>();
        for(NulsThread t:threadMap.values()){
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
