package io.choerodon.devops.infra.util;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.yaml.snakeyaml.introspector.BeanAccess;
import org.yaml.snakeyaml.introspector.Property;
import org.yaml.snakeyaml.introspector.PropertyUtils;

/**
 * 让Snakeyaml获取的字段属性的排序依据Javabean的属性排序
 *
 * @author zmf
 * @since 20-4-3
 */
public class FieldOrderPropertyUtil extends PropertyUtils {
    @Override
    protected Set<Property> createPropertySet(Class<?> type, BeanAccess bAccess) {
        // 获取类的字段，这里是不包含父类的
        Field[] fields = type.getDeclaredFields();
        // 为了set遍历有序
        Set<Property> properties = new LinkedHashSet<>();
        // 从父类中获取集合，这里不重写父类这个方法，是因为无法重写
        Collection<Property> props = getPropertiesMap(type, bAccess).values();

        Map<String, Property> propertyMap = props.stream().collect(Collectors.toMap(Property::getName, Function.identity()));

        // 将此类的字段按照顺序放入set
        for (Field field : fields) {
            Property temp = propertyMap.get(field.getName());
            if (temp == null) {
                continue;
            }
            properties.add(temp);
            propertyMap.remove(field.getName());
        }
        for (Property property : propertyMap.values()) {
            if (property.isReadable()) {
                properties.add(property);
            }
        }
        return properties;
    }
}
