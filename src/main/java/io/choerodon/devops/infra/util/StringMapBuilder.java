package io.choerodon.devops.infra.util;

import java.util.HashMap;
import java.util.Map;

import org.springframework.util.CollectionUtils;

/**
 * 用于方便构建 {@link Map<String String>} 类型的map
 *
 * @author zmf
 * @since 20-5-15
 */
public class StringMapBuilder {
    private final Map<String, String> content;

    private StringMapBuilder() {
        content = new HashMap<>();
    }

    private StringMapBuilder(int initMapSize) {
        content = new HashMap<>(initMapSize);
    }

    public static StringMapBuilder newBuilder(int initMapSize) {
        return new StringMapBuilder(initMapSize);
    }

    public static StringMapBuilder newBuilder() {
        return new StringMapBuilder();
    }

    public StringMapBuilder put(Object key, Object value) {
        content.put(String.valueOf(key), String.valueOf(value));
        return this;
    }

    public StringMapBuilder putAll(Map<?, ?> map) {
        if (!CollectionUtils.isEmpty(map)) {
            map.forEach(this::put);
        }
        return this;
    }

    public Map<String, String> build() {
        return content;
    }
}
