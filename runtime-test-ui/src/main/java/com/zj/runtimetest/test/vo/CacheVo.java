package com.zj.runtimetest.test.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.zj.runtimetest.vo.RequestInfo;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;
import java.util.*;
import java.util.function.Consumer;

/**
 * @author : jie.zhou
 * @date : 2025/6/30
 */
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Data
public class CacheVo extends RequestInfo implements Serializable {

    @Serial
    private static final long serialVersionUID = -574377082031667607L;

    @JsonIgnore
    private Long pid;

    private final int size = 5;

    @JsonIgnore
    private List<String> history;


    public void addHistory(String s) {
        if (Objects.isNull(history)) {
            history = new ArrayList<>();
        }
        if (history.contains(s)) {
            history.remove(s);
            history.add(s);
            return;
        }
        if (history.size() >= size) {
            history.remove(history.get(0));
        }
        history.add(s);
    }

    public void forEachHistory(Consumer<String> action) {
        if (Objects.isNull(history)) {
            history = new ArrayList<>();
        }
        // 倒序
        ArrayList<String> list = new ArrayList<>(history);
        for (int i = list.size() - 1; i >= 0; i--) {
            action.accept(list.get(i));
        }
    }
}
