package io.choerodon.devops.domain.application.Handler;

import java.io.File;
import java.util.List;
import java.util.Map;

import io.kubernetes.client.models.V1Service;
import io.kubernetes.client.models.V1beta1Ingress;

import io.choerodon.devops.domain.application.valueobject.C7nHelmRelease;

public class SerializableChain {


    private SerializableHandler c7NHelmReleaseHandler = new C7NHelmReleaseHandler();
    private SerializableHandler v1ServiceHandler = new V1ServiceHandler();
    private SerializableHandler ingressHandler = new IngressHandler();

    public void createChain() {
        c7NHelmReleaseHandler.setNext(v1ServiceHandler);
        v1ServiceHandler.setNext(ingressHandler);
    }

    public void handler(File file, String filePath, Map<String,String> objectPath, List<C7nHelmRelease> c7nHelmReleases, List<V1Service> v1Services, List<V1beta1Ingress> v1beta1Ingresses) {
         c7NHelmReleaseHandler.handle(file, filePath ,objectPath,c7nHelmReleases,v1Services,v1beta1Ingresses);
    }
}
