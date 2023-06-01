package io.choerodon.devops.infra.util;

import java.util.*;

/**
 * @author superlee
 */
public class C7nCollectionUtils {

    private C7nCollectionUtils() {
        throw new IllegalStateException("cann`t instantiation class");
    }

    /**
     * 将原本的列表按照容量分为多个子列表，对大列表进行分段
     *
     * @param originalList 原始列表
     * @param volume       子列表容量
     * @param <T>          类型
     * @return 子列表的列表
     */
    public static <T> List<List<T>> fragmentList(List<T> originalList, int volume) {
        List<List<T>> list = new ArrayList<>();
        if (volume < 1) {
            return list;
        }
        int size = originalList.size();
        int count = (size % volume == 0) ? size / volume : size / volume + 1;
        int start = 0;
        int end = volume;
        if (size != 0) {
            for (int i = 0; i < count; i++) {
                end = Math.min(end, size);
                List<T> subList = originalList.subList(start, end);
                start = start + volume;
                end = end + volume;
                list.add(subList);
            }
        }
        return list;
    }

    public static <T> List<Set<T>> fragmentSet(Set<T> originalSet, int volume) {
        List<Set<T>> list = new ArrayList<>();
        if (volume < 1) {
            return list;
        }
        int size = originalSet.size();
        int count = (size % volume == 0) ? size / volume : size / volume + 1;
        if (size != 0) {
            Iterator<T> iterator = originalSet.iterator();
            for (int i = 0; i < count; i++) {
                int counter = 0;
                Set<T> set = new HashSet<>();
                list.add(set);
                while (counter < volume) {
                    if (iterator.hasNext()) {
                        set.add(iterator.next());
                    }
                    counter++;
                }
            }
        }
        return list;
    }
}
