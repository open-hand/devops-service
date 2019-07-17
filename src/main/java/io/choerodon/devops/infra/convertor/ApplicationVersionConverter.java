package io.choerodon.devops.infra.convertor;

import io.choerodon.devops.api.vo.ApplicationVersionRespVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.vo.iam.entity.ApplicationVersionE;
import io.choerodon.devops.infra.dto.ApplicationVersionDTO;

@Component
public class ApplicationVersionConverter implements ConvertorI<ApplicationVersionE, ApplicationVersionDTO, ApplicationVersionRespVO> {

    @Override
    public ApplicationVersionE doToEntity(ApplicationVersionDTO applicationVersionDTO) {
        ApplicationVersionE applicationVersionE = ApplicationVersionEFactory.create();
        BeanUtils.copyProperties(applicationVersionDTO, applicationVersionE);
        applicationVersionE.initApplicationE(applicationVersionDTO.getAppId(), applicationVersionDTO.getAppCode(),
                applicationVersionDTO.getAppName(), applicationVersionDTO.getAppStatus());
        applicationVersionE.initApplicationVersionValueE(applicationVersionDTO.getValueId());
        applicationVersionE.initApplicationVersionReadmeVById(applicationVersionDTO.getReadmeValueId());
        return applicationVersionE;
    }

    @Override
    public ApplicationVersionDTO entityToDo(ApplicationVersionE applicationVersionE) {
        ApplicationVersionDTO applicationVersionDTO = new ApplicationVersionDTO();
        BeanUtils.copyProperties(applicationVersionE, applicationVersionDTO);
        applicationVersionDTO.setAppId(applicationVersionE.getApplicationE().getId());
        if (applicationVersionE.getApplicationVersionValueE() != null) {
            applicationVersionDTO.setValueId(applicationVersionE.getApplicationVersionValueE().getId());
        }
        return applicationVersionDTO;
    }

    @Override
    public ApplicationVersionRespVO entityToDto(ApplicationVersionE entity) {
        ApplicationVersionRespVO applicationVersionRespVO = new ApplicationVersionRespVO();
        BeanUtils.copyProperties(entity, applicationVersionRespVO);
        applicationVersionRespVO.setAppId(entity.getApplicationE().getId());
        applicationVersionRespVO.setAppCode(entity.getApplicationE().getCode());
        applicationVersionRespVO.setAppName(entity.getApplicationE().getName());
        applicationVersionRespVO.setAppStatus(entity.getApplicationE().getActive());
        return applicationVersionRespVO;
    }
}
