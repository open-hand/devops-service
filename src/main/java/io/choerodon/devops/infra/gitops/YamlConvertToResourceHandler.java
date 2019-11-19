package io.choerodon.devops.infra.gitops;

import java.util.Map;
import java.util.Objects;

import io.kubernetes.client.JSON;

import io.choerodon.devops.infra.exception.GitOpsExplainException;
import io.choerodon.devops.infra.util.TypeUtil;

public class YamlConvertToResourceHandler<T> {

    private Class<T> targetClass;

    public YamlConvertToResourceHandler(Class<T> targetClass) {
        this.targetClass = Objects.requireNonNull(targetClass);
    }

    public T serializable(String yamlContent,
                          String filePath,
                          Map<String, String> objectPath) {
        JSON json = new JSON();
        T result;
        try {
            result = json.deserialize(yamlContent, targetClass);
        } catch (Exception e) {
            throw new GitOpsExplainException(e.getMessage(), filePath);
        }
        objectPath.put(TypeUtil.objToString(result.hashCode()), filePath);
        return result;
    }
}
