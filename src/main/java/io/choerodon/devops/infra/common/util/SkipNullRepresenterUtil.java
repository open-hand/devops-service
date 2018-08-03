package io.choerodon.devops.infra.common.util;


import io.kubernetes.client.custom.IntOrString;
import org.yaml.snakeyaml.introspector.Property;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

public class SkipNullRepresenterUtil extends Representer {

    @Override
    protected NodeTuple representJavaBeanProperty(Object javaBean, Property property,
                                                  Object propertyValue, Tag customTag) {
        if (propertyValue == null) {
            return null;
        }
        if (property.getType().equals(IntOrString.class)) {
            if (((IntOrString) propertyValue).isInteger()) {
                propertyValue = TypeUtil.objToInteger(propertyValue);
            } else {
                propertyValue = TypeUtil.objToString(propertyValue);
            }
            return super
                    .representJavaBeanProperty(javaBean, property, propertyValue, customTag);
        } else {
            return super
                    .representJavaBeanProperty(javaBean, property, propertyValue, customTag);
        }
    }
}