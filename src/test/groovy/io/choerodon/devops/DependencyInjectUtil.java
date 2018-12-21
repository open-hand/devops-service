package io.choerodon.devops;

import java.lang.reflect.Field;

/**
 * @author zmf
 */
public class DependencyInjectUtil {
    private DependencyInjectUtil() {
    }

    /**
     * set the attribution
     *
     * @param instance       the instance to set value into
     * @param attributeName  the name of the field
     * @param attributeValue the value of the field
     */
    public static void setAttribute(Object instance, String attributeName, Object attributeValue) {
        Class instanceClass = instance.getClass();
        try {
            Field field = instanceClass.getDeclaredField(attributeName);
            field.setAccessible(true);
            field.set(instance, attributeValue);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("通过反射注入依赖失败，检查字段名是否正确");
        }
    }
}
