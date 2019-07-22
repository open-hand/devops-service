package io.choerodon.devops.domain.application.convertor;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.vo.ApplicationInstanceDTO;
import io.choerodon.devops.domain.application.factory.ApplicationInstanceFactory;
import io.choerodon.devops.infra.dataobject.ApplicationInstanceDO;

/**
 * Created by Zenger on 2018/4/2.
 */
@Component
public class ApplicationInstanceConvertor implements ConvertorI<ApplicationInstanceE, ApplicationInstanceDO, ApplicationInstanceDTO> {

    @Override
    public ApplicationInstanceE doToEntity(ApplicationInstanceDO applicationInstanceDO) {
        ApplicationInstanceE applicationInstanceE = ApplicationInstanceFactory.create();
        BeanUtils.copyProperties(applicationInstanceDO, applicationInstanceE);
        applicationInstanceE.initApplicationE(applicationInstanceDO.getAppId(), applicationInstanceDO.getAppName());
        if (applicationInstanceDO.getAppVersionId() != null) {
            applicationInstanceE.initApplicationVersionE(
                    applicationInstanceDO.getAppVersionId(), applicationInstanceDO.getAppVersion());
        }
        applicationInstanceE.initDevopsEnvironmentE(
                applicationInstanceDO.getEnvId(),
                applicationInstanceDO.getEnvCode(),
                applicationInstanceDO.getEnvName());
        return applicationInstanceE;
    }


    @Override
    public ApplicationInstanceDO entityToDo(ApplicationInstanceE applicationInstanceE) {
        ApplicationInstanceDO applicationInstanceDO = new ApplicationInstanceDO();
        BeanUtils.copyProperties(applicationInstanceE, applicationInstanceDO);
        applicationInstanceDO.setAppId(applicationInstanceE.getApplicationE().getId());
        if (applicationInstanceE.getApplicationVersionE() != null) {
            applicationInstanceDO.setAppVersionId(applicationInstanceE.getApplicationVersionE().getId());
        }
        applicationInstanceDO.setEnvId(applicationInstanceE.getDevopsEnvironmentE().getId());
        return applicationInstanceDO;
    }

    @Override
    public ApplicationInstanceDTO entityToDto(ApplicationInstanceE entity) {
        ApplicationInstanceDTO applicationDeployDTO = new ApplicationInstanceDTO();
        BeanUtils.copyProperties(entity, applicationDeployDTO);
        applicationDeployDTO.setAppName(entity.getApplicationE().getName());
        applicationDeployDTO.setEnvCode(entity.getDevopsEnvironmentE().getCode());
        applicationDeployDTO.setEnvName(entity.getDevopsEnvironmentE().getName());
        applicationDeployDTO.setAppId(entity.getApplicationE().getId());
        applicationDeployDTO.setEnvId(entity.getDevopsEnvironmentE().getId());
        if (entity.getApplicationVersionE() != null) {
            applicationDeployDTO.setAppVersionId(entity.getApplicationVersionE().getId());
            applicationDeployDTO.setAppVersion(entity.getApplicationVersionE().getVersion());
        }
        return applicationDeployDTO;
    }
}
