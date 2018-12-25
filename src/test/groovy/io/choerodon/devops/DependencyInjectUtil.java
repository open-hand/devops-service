package io.choerodon.devops;

import io.choerodon.core.convertor.ApplicationContextHelper;

import java.lang.reflect.Field;

/**
 * @author zmf
 */
public class DependencyInjectUtil {
    private static final String SPRING_PROXY_CLASS = "EnhancerBySpringCGLIB";

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
        Class<?> instanceClass = instance.getClass();
        if (instanceClass.getTypeName().contains(SPRING_PROXY_CLASS)) {
            instanceClass = instanceClass.getSuperclass();
        }
        try {
            Field field = instanceClass.getDeclaredField(attributeName);
            field.setAccessible(true);
            field.set(instance, attributeValue);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("通过反射注入依赖失败，检查字段名是否正确");
        }
    }

    /**
     * restore the dependency to the default
     *
     * @param instance      the instance to reset
     * @param attributeName the name of the field
     */
    public static void restoreDefaultDependency(Object instance, String attributeName) {
        Class instanceClass = instance.getClass();
        try {
            Field field = instanceClass.getDeclaredField(attributeName);
            Object defaultDependency = ApplicationContextHelper.getSpringFactory().getBean(field.getType());
            setAttribute(instance, attributeName, defaultDependency);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException("通过反射重置依赖失败，检查字段名是否正确");
        }
    }
}
