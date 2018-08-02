package io.choerodon.devops.domain.application.handler;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import java.util.Map;

import io.kubernetes.client.models.V1Service;
import io.kubernetes.client.models.V1beta1Ingress;
import org.yaml.snakeyaml.Yaml;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.domain.application.entity.DevopsEnvFileLogE;
import io.choerodon.devops.domain.application.valueobject.C7nHelmRelease;
import io.choerodon.devops.infra.common.util.TypeUtil;

public class IngressHandler extends SerializableHandler {

    @Override
    public void handle(File file, String filePath, Map<String, String> objectPath, List<C7nHelmRelease> c7nHelmReleases, List<V1Service> v1Services, List<V1beta1Ingress> v1beta1Ingresses, DevopsEnvFileLogE devopsEnvFileLogE) {
        Yaml yaml = new Yaml();
        V1beta1Ingress ingress = null;
        try {
            ingress = yaml.loadAs(new FileInputStream(file), V1beta1Ingress.class);
        } catch (Exception e) {
            devopsEnvFileLogE.setMessage(e.getMessage());
            devopsEnvFileLogE.setFilePath(filePath);
            throw new CommonException("error.file.serializable");
        }
        objectPath.put(TypeUtil.objToString(v1beta1Ingresses.hashCode()), filePath);
        v1beta1Ingresses.add(ingress);

    }
}
