package io.choerodon.devops.app.service.impl;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.devops.app.service.DevopsEnvFileResourceService;
import io.choerodon.devops.api.vo.iam.entity.DevopsEnvFileResourceE;
import io.choerodon.devops.api.vo.iam.entity.DevopsEnvironmentE;
import io.choerodon.devops.domain.application.repository.DevopsEnvFileResourceRepository;
import io.choerodon.devops.infra.util.TypeUtil;

@Service
public class DevopsEnvFileResourceServiceImpl implements DevopsEnvFileResourceService {

    @Autowired
    private DevopsEnvFileResourceRepository devopsEnvFileResourceRepository;


    @Override
    public void updateOrCreateFileResource(Map<String, String> objectPath,
                                            Long envId,
                                            DevopsEnvFileResourceE devopsEnvFileResourceE,
                                            Integer i, Long id, String kind) {
        if (devopsEnvFileResourceE != null) {
            devopsEnvFileResourceE.setFilePath(objectPath.get(
                    TypeUtil.objToString(i)));
            devopsEnvFileResourceRepository.updateFileResource(devopsEnvFileResourceE);
        } else {
            devopsEnvFileResourceE = new DevopsEnvFileResourceE();
            devopsEnvFileResourceE.setEnvironment(new DevopsEnvironmentE(envId));
            devopsEnvFileResourceE.setFilePath(objectPath.get(
                    TypeUtil.objToString(i)));
            devopsEnvFileResourceE.setResourceId(id);
            devopsEnvFileResourceE.setResourceType(kind);
            devopsEnvFileResourceRepository.createFileResource(devopsEnvFileResourceE);
        }
    }
}
