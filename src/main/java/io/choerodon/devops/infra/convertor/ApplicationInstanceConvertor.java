package io.choerodon.devops.infra.convertor;

import io.choerodon.devops.api.vo.ApplicationInstanceVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.vo.iam.entity.ApplicationInstanceE;
import io.choerodon.devops.infra.dto.ApplicationInstanceDTO;

/**
 * Created by Zenger on 2018/4/2.
 */
@Component
public class ApplicationInstanceConvertor implements ConvertorI<Object, ApplicationInstanceDTO, ApplicationInstanceVO> {

    @Override
    public ApplicationInstanceE doToEntity(ApplicationInstanceDTO applicationInstanceDTO) {
        ApplicationInstanceE applicationInstanceE = ApplicationInstanceFactory.create();
        BeanUtils.copyProperties(applicationInstanceDTO, applicationInstanceE);
        applicationInstanceE.initApplicationE(applicationInstanceDTO.getAppId(), applicationInstanceDTO.getAppName());
        if (applicationInstanceDTO.getAppVersionId() != null) {
            applicationInstanceE.initApplicationVersionE(
                    applicationInstanceDTO.getAppVersionId(), applicationInstanceDTO.getAppVersion());
        }
        applicationInstanceE.initDevopsEnvironmentE(
                applicationInstanceDTO.getEnvId(),
                applicationInstanceDTO.getEnvCode(),
                applicationInstanceDTO.getEnvName());
        return applicationInstanceE;
    }


    @Override
    public ApplicationInstanceDTO entityToDo(ApplicationInstanceE applicationInstanceE) {
        ApplicationInstanceDTO applicationInstanceDTO = new ApplicationInstanceDTO();
        BeanUtils.copyProperties(applicationInstanceE, applicationInstanceDTO);
        applicationInstanceDTO.setAppId(applicationInstanceE.getApplicationE().getId());
        if (applicationInstanceE.getApplicationVersionE() != null) {
            applicationInstanceDTO.setAppVersionId(applicationInstanceE.getApplicationVersionE().getId());
        }
        applicationInstanceDTO.setEnvId(applicationInstanceE.getDevopsEnvironmentE().getId());
        return applicationInstanceDTO;
    }

    @Override
    public ApplicationInstanceVO entityToDto(ApplicationInstanceE entity) {
        ApplicationInstanceVO applicationDeployDTO = new ApplicationInstanceVO();
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
