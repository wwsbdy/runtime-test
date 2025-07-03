package com.zj.runtimetest.test.vo;

import org.apache.commons.collections.CollectionUtils;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.function.Consumer;

/**
 * @author 19242
 */
public class HistoryList<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 9025082433851705053L;
    private final int size;
    private final LinkedHashSet<T> set = new LinkedHashSet<>();

    public HistoryList(int size) {
        this.size = size;
    }

    public boolean add(T s) {
        if (set.contains(s)) {
            set.remove(s);
            set.add(s);
            return true;
        }
        if (set.size() >= size) {
            set.remove(set.iterator().next());
        }
        return set.add(s);
    }

    public void forEach(Consumer<? super T> action) {
        // 倒序
        ArrayList<T> list = new ArrayList<>(set);
        for (int i = list.size() - 1; i >= 0; i--) {
            action.accept(list.get(i));
        }
    }

    public boolean isNotEmpty() {
        return CollectionUtils.isNotEmpty(set);
    }
}
