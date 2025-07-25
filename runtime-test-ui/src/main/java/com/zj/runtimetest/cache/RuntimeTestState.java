package com.zj.runtimetest.cache;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.zj.runtimetest.vo.CacheVo;
import com.zj.runtimetest.vo.ProcessVo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author : jie.zhou
 * @date : 2025/6/12
 */
@State(name = "RuntimeTestState", storages = @Storage("RuntimeTestState.xml"))
public class RuntimeTestState implements PersistentStateComponent<RuntimeTestState> {

    /**
     * 注意这个属性必须是public
     */
    public Map<String, CacheVo> cache = new ConcurrentHashMap<>();

    private final Map<Long, ProcessVo> pidProcessMap = new LinkedHashMap<>();

    public static RuntimeTestState getInstance(Project project) {
        return project.getService(RuntimeTestState.class);
    }

    @Override
    public @Nullable RuntimeTestState getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull RuntimeTestState state) {
        cache.putAll(state.cache);
    }

    public void putCache(String key, CacheVo value) {
        if (Objects.isNull(value)) {
            return;
        }
        cache.put(key, value);
    }

    public CacheVo getCache(String key) {
        return cache.get(key);
    }

    public void putPidProcessMap(Long pid, ProcessVo process) {
        pidProcessMap.put(pid, process);
    }

    public ProcessVo getProcess(Long pid) {
        return pidProcessMap.get(pid);
    }

    public void removePidProcessMap(Long pid) {
        pidProcessMap.remove(pid);
    }

    public boolean containsPid(Long pid) {
        return pidProcessMap.containsKey(pid);
    }

    public Set<Long> getPids() {
        return pidProcessMap.keySet();
    }
}

