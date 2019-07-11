package io.choerodon.devops.app.service.impl;

import java.util.List;
import java.util.Map;

import io.choerodon.devops.domain.application.entity.DevopsEnvFileResourceE;
import io.choerodon.devops.infra.gitops.YamlConvertToResourceHandler;

public abstract class ConvertK8sObjectService<T> {

    private T t;

    public T serializableObject(String jsonString, String filePath, Map<String, String> objectPath) {
        YamlConvertToResourceHandler<T> yamlConvertToResourceHandler
                = new YamlConvertToResourceHandler<>();
        yamlConvertToResourceHandler.setT(t);
        t = yamlConvertToResourceHandler
                .serializable(jsonString, filePath, objectPath);
        return t;
    }

    public void checkParameters(T t, Map<String,String> objectPath) {
    }


    public void checkIfExist(List<T> ts, Long envId, List<DevopsEnvFileResourceE> beforeSyncDelete, Map<String, String> objectPath, T t) {
    }

    public T getT() {
        return t;
    }

    public void setT(T t) {
        this.t = t;
    }
}
