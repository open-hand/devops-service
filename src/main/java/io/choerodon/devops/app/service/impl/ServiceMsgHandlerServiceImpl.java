package io.choerodon.devops.app.service.impl;

import java.util.Arrays;
import java.util.List;

import io.kubernetes.client.JSON;
import io.kubernetes.client.models.V1Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import io.choerodon.devops.app.service.ServiceMsgHandlerService;
import io.choerodon.devops.domain.application.entity.*;
import io.choerodon.devops.domain.application.factory.DevopsInstanceResourceFactory;
import io.choerodon.devops.domain.application.repository.*;
import io.choerodon.devops.infra.common.util.TypeUtil;
import io.choerodon.devops.infra.common.util.enums.CommandStatus;
import io.choerodon.devops.infra.common.util.enums.ObjectType;
import io.choerodon.devops.infra.common.util.enums.ServiceStatus;
import io.choerodon.websocket.tool.KeyParseTool;

/**
 * Created by Zenger on 2018/4/21.
 */
@Service
public class ServiceMsgHandlerServiceImpl implements ServiceMsgHandlerService {

    private static final Logger logger = LoggerFactory.getLogger(ServiceMsgHandlerServiceImpl.class);
    private static JSON json = new JSON();
    private DevopsServiceRepository devopsServiceRepository;
    private ApplicationInstanceRepository applicationInstanceRepository;
    private DevopsEnvResourceRepository devopsEnvResourceRepository;
    private DevopsEnvResourceDetailRepository devopsEnvResourceDetailRepository;
    private DevopsEnvCommandRepository devopsEnvCommandRepository;


    public ServiceMsgHandlerServiceImpl(DevopsServiceRepository devopsServiceRepository,
                                        ApplicationInstanceRepository applicationInstanceRepository,
                                        DevopsEnvResourceRepository devopsEnvResourceRepository,
                                        DevopsEnvResourceDetailRepository devopsEnvResourceDetailRepository,
                                        DevopsEnvCommandRepository devopsEnvCommandRepository) {
        this.devopsServiceRepository = devopsServiceRepository;
        this.applicationInstanceRepository = applicationInstanceRepository;
        this.devopsEnvResourceDetailRepository = devopsEnvResourceDetailRepository;
        this.devopsEnvResourceRepository = devopsEnvResourceRepository;
        this.devopsEnvCommandRepository = devopsEnvCommandRepository;
    }

    @Override
    public void handlerServiceCreateMessage(String key, String msg) {
        DevopsServiceE devopsServiceE = devopsServiceRepository.selectByNameAndNamespace(
                KeyParseTool.getResourceName(key), KeyParseTool.getNamespace(key));
        try {
            V1Service v1Service = json.deserialize(msg, V1Service.class);
            String releaseNames = v1Service.getMetadata().getAnnotations()
                    .get("choerodon.io/network-service-instances");
            List<String> releases = Arrays.asList(releaseNames.split("\\+"));
            DevopsEnvResourceE devopsEnvResourceE =
                    DevopsInstanceResourceFactory.createDevopsInstanceResourceE();
            DevopsEnvResourceDetailE devopsEnvResourceDetailE = new DevopsEnvResourceDetailE();
            devopsEnvResourceDetailE.setMessage(msg);
            devopsEnvResourceE.setKind(KeyParseTool.getResourceType(key));
            devopsEnvResourceE.setName(v1Service.getMetadata().getName());
            devopsEnvResourceE.setReversion(TypeUtil.objToLong(v1Service.getMetadata().getResourceVersion()));
            for (String release : releases) {
                ApplicationInstanceE applicationInstanceE = applicationInstanceRepository
                        .selectByCode(release);

                DevopsEnvResourceE newdevopsEnvResourceE = devopsEnvResourceRepository
                        .queryByInstanceIdAndKindAndName(
                                applicationInstanceE.getId(),
                                KeyParseTool.getResourceType(key),
                                KeyParseTool.getResourceName(key));
                saveOrUpdateResource(devopsEnvResourceE,
                        newdevopsEnvResourceE,
                        devopsEnvResourceDetailE,
                        applicationInstanceE);
            }
            devopsServiceE.setStatus(ServiceStatus.RUNNING.getStatus());
            devopsServiceRepository.update(devopsServiceE);
            DevopsEnvCommandE newdevopsEnvCommandE = devopsEnvCommandRepository
                    .queryByObject(ObjectType.SERVICE.getObjectType(), devopsServiceE.getId());
            newdevopsEnvCommandE.setStatus(CommandStatus.SUCCESS.getCommandStatus());
            devopsEnvCommandRepository.update(newdevopsEnvCommandE);
        } catch (Exception e) {
            logger.info(e.getMessage());
        }
    }

    private void saveOrUpdateResource(DevopsEnvResourceE devopsEnvResourceE,
                                      DevopsEnvResourceE newdevopsEnvResourceE,
                                      DevopsEnvResourceDetailE devopsEnvResourceDetailE,
                                      ApplicationInstanceE applicationInstanceE) {
        if (newdevopsEnvResourceE == null) {
            devopsEnvResourceE.initDevopsInstanceResourceMessageE(
                    devopsEnvResourceDetailRepository.create(devopsEnvResourceDetailE).getId());
            if (!devopsEnvResourceE.getKind().equals("Ingress")) {
                devopsEnvResourceE.initApplicationInstanceE(applicationInstanceE.getId());
            }
            devopsEnvResourceRepository.create(devopsEnvResourceE);
            return;
        }
        if (newdevopsEnvResourceE.getReversion() == null) {
            newdevopsEnvResourceE.setReversion(0L);
        }
        if (devopsEnvResourceE.getReversion() == null) {
            devopsEnvResourceE.setReversion(0L);
        }
        if (!newdevopsEnvResourceE.getReversion().equals(devopsEnvResourceE.getReversion())) {
            newdevopsEnvResourceE.setReversion(devopsEnvResourceE.getReversion());
            devopsEnvResourceDetailE.setId(
                    newdevopsEnvResourceE.getDevopsEnvResourceDetailE().getId());
            devopsEnvResourceRepository.update(newdevopsEnvResourceE);
            devopsEnvResourceDetailRepository.update(devopsEnvResourceDetailE);
        }
    }
}
