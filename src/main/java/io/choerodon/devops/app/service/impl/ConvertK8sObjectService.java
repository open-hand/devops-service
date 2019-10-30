package io.choerodon.devops.app.service.impl;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import io.choerodon.devops.infra.dto.DevopsEnvFileResourceDTO;
import io.choerodon.devops.infra.gitops.YamlConvertToResourceHandler;

public abstract class ConvertK8sObjectService<T> {
    private Class<T> targetClass;

    public ConvertK8sObjectService(Class<T> targetClass) {
        this.targetClass = Objects.requireNonNull(targetClass);
    }

    public T serializableObject(String jsonString, String filePath, Map<String, String> objectPath) {
        YamlConvertToResourceHandler<T> yamlConvertToResourceHandler
                = new YamlConvertToResourceHandler<>(targetClass);
        return yamlConvertToResourceHandler
                .serializable(jsonString, filePath, objectPath);
    }

    public void checkParameters(T t, Map<String,String> objectPath) {
    }


    public void checkIfExist(List<T> ts, Long envId, List<DevopsEnvFileResourceDTO> beforeSyncDelete, Map<String, String> objectPath, T t) {
    }
}
