package io.choerodon.devops.domain.application.handler;

import java.util.Map;

import io.kubernetes.client.JSON;
import org.yaml.snakeyaml.Yaml;

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
                          Map<String, String> objectPath) {
        JSON json = new JSON();
        try {
            t = (T) json.deserialize(yamlContent, t.getClass());
        } catch (Exception e) {
            throw new GitOpsExplainException(e.getMessage(),filePath);
        }
        objectPath.put(TypeUtil.objToString(t.hashCode()), filePath);
        return t;
    }

}
