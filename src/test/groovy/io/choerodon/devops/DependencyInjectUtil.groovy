package io.choerodon.devops

import io.choerodon.core.convertor.ApplicationContextHelper
import org.springframework.aop.TargetSource
import org.springframework.aop.framework.AdvisedSupport
import org.springframework.util.ReflectionUtils

import java.lang.reflect.Field

/**
 * This utility is not completely tested.
 * Please do not use this other than unit test code.
 * Please do not use this other than unit test code.
 * Please do not use this other than unit test code.
 *
 * @author zmf
 */
class DependencyInjectUtil {
    private static final String SPRING_PROXY_CLASS = "EnhancerBySpringCGLIB"
    private static final String DYNAMIC_ADVISED_INTERCEPTOR = 'CGLIB$CALLBACK_0'

    private DependencyInjectUtil() {
    }

    /**
     * set the attribution
     *
     * @param instance       the instance to set value into
     * @param attributeName  the name of the field
     * @param attributeValue the value of the field
     */
    static void setAttribute(Object instance, String attributeName, Object attributeValue) {
        Class<?> instanceClass = instance.getClass()
        if (instanceClass.getTypeName().contains(SPRING_PROXY_CLASS)) {
            setAttributeOfSpringAopObject(instance, attributeName, attributeValue)
            return
        }
        try {
            Field field = instanceClass.getDeclaredField(attributeName)
            ReflectionUtils.makeAccessible(field)
            field.set(instance, attributeValue)
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Failure for dependency injection by reflection. Please check whether the field name exists.")
        }
    }

    /**
     * set the attribute of spring aop object by cglib.
     *
     * @param instance       the instance of the aop agent instance
     * @param attributeName  the name of the field
     * @param attributeValue the value of the field
     * @see org.springframework.aop.framework.CglibAopProxy#getCallbacks(Class)
     * @see org.springframework.aop.framework.CglibAopProxy.DynamicAdvisedInterceptor
     * @see AdvisedSupport#getTargetSource()
     * @see TargetSource#getTarget()
     */
    private static void setAttributeOfSpringAopObject(Object instance, String attributeName, Object attributeValue) {
        try {
            Field[] fields = instance.getClass().getDeclaredFields()
            Field dynamicAdvisedInterceptor = null

            // get certain field
            for (Field field : fields) {
                if (field.getName() == DYNAMIC_ADVISED_INTERCEPTOR) {
                    dynamicAdvisedInterceptor = field
                    break
                }
            }

            if (dynamicAdvisedInterceptor != null) {
                ReflectionUtils.makeAccessible(dynamicAdvisedInterceptor)
                Object dynamicAdvisedInterceptorInstance = dynamicAdvisedInterceptor.get(instance)
                Field field = dynamicAdvisedInterceptorInstance.getClass().getDeclaredField("advised")
                ReflectionUtils.makeAccessible(field)
                AdvisedSupport advisedSupport = (AdvisedSupport) field.get(dynamicAdvisedInterceptorInstance)
                Object actualTarget = advisedSupport.getTargetSource().getTarget()
                setAttribute(actualTarget, attributeName, attributeValue)
            } else {
                throw new RuntimeException("This class can not be set attribute")
            }
        } catch (Exception e) {
            throw new RuntimeException("This class can not be set attribute: {}", e)
        }
    }

    /**
     * restore the dependency to the default
     *
     * @param instance      the instance to reset
     * @param attributeName the name of the field
     */
    static void restoreDefaultDependency(Object instance, String attributeName) {
        Class<?> instanceClass = instance.getClass()
        if (instanceClass.getTypeName().contains(SPRING_PROXY_CLASS)) {
            instanceClass = instanceClass.getSuperclass()
        }
        try {
            Field field = instanceClass.getDeclaredField(attributeName)
            Object defaultDependency = ApplicationContextHelper.getSpringFactory().getBean(field.getType())
            setAttribute(instance, attributeName, defaultDependency)
        } catch (NoSuchFieldException e) {
            throw new RuntimeException("Failure for dependency reset by reflection. Please check whether the field name exists.")
        }
    }
}
