package io.choerodon.devops.domain.service;

import java.util.List;
import java.util.Map;

import io.choerodon.devops.domain.application.entity.DevopsEnvFileResourceE;
import io.choerodon.devops.domain.application.handler.SerializableOperation;

public abstract class ConvertK8sObjectService<T> {

    private T t;


    public T SerializableObject(String jsonString, String filePath, Map<String, String> objectPath) {
        SerializableOperation<T> serializableOperation
                = new SerializableOperation<>();
        serializableOperation.setT(t);
        t = serializableOperation
                .serializable(jsonString, filePath, objectPath);
        return t;
    }

    public void checkParameters(T t, Map<String,String> objectPath) {
    }


    public void checkIfexist(List<T> ts, Long envId, List<DevopsEnvFileResourceE> beforeSyncDelete, Map<String, String> objectPath, T t) {
    }

    public T getT() {
        return t;
    }

    public void setT(T t) {
        this.t = t;
    }
}
