package io.choerodon.devops.domain.application.Handler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.List;
import java.util.Map;

import com.esotericsoftware.yamlbeans.YamlReader;
import io.kubernetes.client.models.V1Service;
import io.kubernetes.client.models.V1beta1Ingress;
import org.yaml.snakeyaml.Yaml;

import io.choerodon.devops.domain.application.valueobject.C7nHelmRelease;
import io.choerodon.devops.infra.common.util.TypeUtil;

public class C7NHelmReleaseHandler extends SerializableHandler {

    @Override
    public void handle(File file, String filePath,Map<String,String> objectPath, List<C7nHelmRelease> c7nHelmReleases, List<V1Service> v1Services, List<V1beta1Ingress> v1beta1Ingresses) {
        Yaml yaml = new Yaml();
        C7nHelmRelease c7nHelmRelease = null;
        try {
            c7nHelmRelease = yaml.loadAs(new FileInputStream(file),C7nHelmRelease.class);
        } catch (Exception e) {
            getNext().handle(file,filePath,objectPath,c7nHelmReleases,v1Services,v1beta1Ingresses);
        }
        objectPath.put(TypeUtil.objToString(c7nHelmRelease.hashCode()),filePath);
        c7nHelmReleases.add(c7nHelmRelease);
    }
}
