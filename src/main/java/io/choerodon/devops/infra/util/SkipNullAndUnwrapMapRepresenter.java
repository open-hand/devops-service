package io.choerodon.devops.infra.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.introspector.Property;
import org.yaml.snakeyaml.nodes.*;

import io.choerodon.devops.infra.annotation.YamlUnwrapped;

/**
 * 为了支持 {@link YamlUnwrapped} 注解来对对象的Yaml格式序列化
 *
 * @author zmf
 * @since 20-4-2
 */
public class SkipNullAndUnwrapMapRepresenter extends SkipNullRepresenterUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(SkipNullAndUnwrapMapRepresenter.class);

    @Override
    protected MappingNode representJavaBean(Set<Property> properties, Object javaBean) {
        List<NodeTuple> value = new ArrayList<>(properties.size());
        Tag tag;
        Tag customTag = classTags.get(javaBean.getClass());
        // 用内置的tag，获取到JavaBean就不需要将显式tag替换了
        tag = customTag != null ? customTag : Tag.MAP;
        // flow style will be chosen by BaseRepresenter
        MappingNode node = new MappingNode(tag, value, DumperOptions.FlowStyle.AUTO);
        representedObjects.put(javaBean, node);
        DumperOptions.FlowStyle bestStyle = DumperOptions.FlowStyle.FLOW;
        for (Property property : properties) {
            Object memberValue = property.get(javaBean);
            Tag customPropertyTag = memberValue == null ? null
                    : classTags.get(memberValue.getClass());
            Annotation yamlUnwrapped = null;
            try {
                // 目前只支持Map类型
                if (Map.class.equals(property.getType())) {
                    Field field = javaBean.getClass().getDeclaredField(property.getName());
                    yamlUnwrapped = field.getAnnotation(YamlUnwrapped.class);
                }
            } catch (NoSuchFieldException e) {
                LOGGER.info("SkipNullAndUnwrapMapRepresenter: NoSuchFieldException: class: {}, field: {}", javaBean.getClass().getName(), property.getName());
            }
            if (yamlUnwrapped != null) {
                // 将内部的元素展开到这个层次
                Map<?, ?> mapping = (Map<?, ?>) memberValue;
                // 如果Map为空，忽略这个字段
                if (memberValue == null) {
                    continue;
                }
                for (Map.Entry<?, ?> entry : mapping.entrySet()) {
                    Node nodeKey = representData(entry.getKey());
                    Node nodeValue = representData(entry.getValue());
                    // 覆盖掉nodeValue的表示对象类型的tag. 这里没有对Map的value进行类型判断，可能有优化的空间
                    nodeValue.setTag(Tag.MAP);
                    if (!(nodeKey instanceof ScalarNode && ((ScalarNode) nodeKey).isPlain())) {
                        bestStyle = DumperOptions.FlowStyle.BLOCK;
                    }
                    if (!(nodeValue instanceof ScalarNode && ((ScalarNode) nodeValue).isPlain())) {
                        bestStyle = DumperOptions.FlowStyle.BLOCK;
                    }
                    value.add(new NodeTuple(nodeKey, nodeValue));
                }
            } else {
                NodeTuple tuple = representJavaBeanProperty(javaBean, property, memberValue,
                        customPropertyTag);
                if (tuple == null) {
                    continue;
                }
                if (!((ScalarNode) tuple.getKeyNode()).isPlain()) {
                    bestStyle = DumperOptions.FlowStyle.BLOCK;
                }
                Node nodeValue = tuple.getValueNode();
                if (!(nodeValue instanceof ScalarNode && ((ScalarNode) nodeValue).isPlain())) {
                    bestStyle = DumperOptions.FlowStyle.BLOCK;
                }
                value.add(tuple);
            }
        }
        if (defaultFlowStyle != DumperOptions.FlowStyle.AUTO) {
            node.setFlowStyle(defaultFlowStyle);
        } else {
            node.setFlowStyle(bestStyle);
        }
        return node;
    }
}
