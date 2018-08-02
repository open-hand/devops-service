package io.choerodon.devops.domain.application.handler;

import java.io.File;
import java.util.List;
import java.util.Map;

import io.kubernetes.client.models.V1Service;
import io.kubernetes.client.models.V1beta1Ingress;

import io.choerodon.devops.domain.application.entity.DevopsEnvFileLogE;
import io.choerodon.devops.domain.application.valueobject.C7nHelmRelease;

public abstract class SerializableHandler {

    private SerializableHandler serializableHandler;

    public SerializableHandler getNext() {
        return serializableHandler;
    }

    public void setNext(SerializableHandler serializableHandler) {
        this.serializableHandler = serializableHandler;
    }

    public abstract void handle(File file, String filePath, Map<String, String> objectPath, List<C7nHelmRelease> c7nHelmReleases, List<V1Service> v1Services, List<V1beta1Ingress> v1beta1Ingresses, DevopsEnvFileLogE devopsEnvFileLogE);
}
