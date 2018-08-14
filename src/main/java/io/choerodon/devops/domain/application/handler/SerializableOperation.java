package io.choerodon.devops.domain.application.handler;

import java.util.Map;

import org.yaml.snakeyaml.Yaml;

import io.choerodon.core.convertor.ApplicationContextHelper;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.domain.application.entity.DevopsEnvFileErrorE;
import io.choerodon.devops.domain.application.repository.DevopsEnvFileErrorRepository;
import io.choerodon.devops.infra.common.util.TypeUtil;

public class SerializableOperation<T> {

    private T t;

    public T getT() {
        return t;
    }

    public void setT(T t) {
        this.t = t;
    }

    public T serializable(String yamlContent,
                          String filePath,
                          Map<String, String> objectPath,
                          DevopsEnvFileErrorE devopsEnvFileErrorE) {
        Yaml yaml = new Yaml();
        DevopsEnvFileErrorRepository devopsEnvFileErrorRepository = ApplicationContextHelper.getSpringFactory().getBean(DevopsEnvFileErrorRepository.class);
        try {
            t = (T) yaml.loadAs(yamlContent, t.getClass());
        } catch (Exception e) {
            devopsEnvFileErrorE.setError(devopsEnvFileErrorE.getError() + e.getMessage());
            devopsEnvFileErrorRepository.create(devopsEnvFileErrorE);
            throw new CommonException(e.getMessage());
        }
        objectPath.put(TypeUtil.objToString(t.hashCode()), filePath);
        return t;
    }
}
