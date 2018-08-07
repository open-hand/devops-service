package io.choerodon.devops.domain.application.handler;

import java.util.Map;

import org.yaml.snakeyaml.Yaml;

import io.choerodon.devops.domain.application.entity.DevopsEnvFileE;
import io.choerodon.devops.infra.common.util.TypeUtil;

public class SerializableOperation<T> {

    private T t;

    public T getT() {
        return t;
    }

    public void setT(T t) {
        this.t = t;
    }

    public T serializable(String yamlContent,
                          String filePath,
                          Map<String, String> objectPath,
                          DevopsEnvFileE devopsEnvFileE) {
        Yaml yaml = new Yaml();
        try {
            t = (T) yaml.loadAs(yamlContent, t.getClass());
        } catch (Exception e) {
            devopsEnvFileE.setMessage(devopsEnvFileE.getMessage() + "\n" + e.getMessage());
        }
        objectPath.put(TypeUtil.objToString(t.hashCode()), filePath);
        return t;
    }
}
