package io.choerodon.devops.infra.util;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.util.Assert;

import io.choerodon.core.exception.CommonException;

/**
 * @author zmf
 * @since 20-5-8
 */
public final class JsonHelper {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private JsonHelper() {
    }

    static {
        OBJECT_MAPPER.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        OBJECT_MAPPER.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
    }

    /**
     * 通过jackson反序列化对象
     *
     * @param json json内容
     * @param type 类型
     * @param <T>  泛型
     * @return 对象
     */
    public static <T> T unmarshalByJackson(String json, Class<T> type) {
        Assert.hasLength(json, "JSON to be unmarshalled should not be empty");
        Assert.notNull(type, "Type should not be null");
        try {
            return OBJECT_MAPPER.readValue(json, type);
        } catch (IOException e) {
            throw new CommonException("Failed to unmarshal by jackson. It's unexpected and may be an internal error. The json is: " + json, e);
        }
    }

    /**
     * 通过jackson序列化对象
     *
     * @param object 非空对象
     * @return JSON字符串
     */
    public static String marshalByJackson(Object object) {
        Assert.notNull(object, "Object to be marshaled should not be null");
        try {
            return OBJECT_MAPPER.writeValueAsString(object);
        } catch (IOException e) {
            throw new CommonException("Failed to marshal by jackson. It's unexpected and may be an internal error. The object is: " + object.toString(), e);
        }
    }
}
