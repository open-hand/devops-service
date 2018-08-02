package io.choerodon.devops.domain.application.Handler;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import java.util.Map;

import io.kubernetes.client.models.V1Service;
import io.kubernetes.client.models.V1beta1Ingress;
import org.yaml.snakeyaml.Yaml;

import io.choerodon.devops.domain.application.entity.DevopsEnvFileLogE;
import io.choerodon.devops.domain.application.valueobject.C7nHelmRelease;
import io.choerodon.devops.infra.common.util.TypeUtil;

public class V1ServiceHandler extends SerializableHandler {
    @Override
    public void handle(File file, String filePath, Map<String, String> objectPath, List<C7nHelmRelease> c7nHelmReleases, List<V1Service> v1Services, List<V1beta1Ingress> v1beta1Ingresses, DevopsEnvFileLogE devopsEnvFileLogE) {
        Yaml yaml = new Yaml();
        V1Service v1Service = null;
        try {
            yaml.loadAs(new FileInputStream(file), V1Service.class);
        } catch (Exception e) {
            getNext().handle(file, filePath, objectPath, c7nHelmReleases, v1Services, v1beta1Ingresses, devopsEnvFileLogE);
        }
        objectPath.put(TypeUtil.objToString(v1Service.hashCode()), filePath);
        v1Services.add(v1Service);
    }
}
