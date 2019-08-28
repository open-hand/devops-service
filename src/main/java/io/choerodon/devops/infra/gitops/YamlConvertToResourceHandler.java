package io.choerodon.devops.infra.gitops;

import java.util.Map;

import io.kubernetes.client.JSON;

import io.choerodon.devops.infra.exception.GitOpsExplainException;
import io.choerodon.devops.infra.util.TypeUtil;

public class YamlConvertToResourceHandler<T> {

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
            t = json.deserialize(yamlContent, t.getClass());
        } catch (Exception e) {
            throw new GitOpsExplainException(e.getMessage(), filePath);
        }
        objectPath.put(TypeUtil.objToString(t.hashCode()), filePath);
        return t;
    }

}
