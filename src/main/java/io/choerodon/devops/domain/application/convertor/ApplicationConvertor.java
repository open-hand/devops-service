package io.choerodon.devops.domain.application.convertor;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.dto.ApplicationReqDTO;
import io.choerodon.devops.domain.application.entity.ApplicationE;
import io.choerodon.devops.domain.application.factory.ApplicationFactory;
import io.choerodon.devops.infra.dataobject.ApplicationDO;

/**
 * Created by Zenger on 2018/4/2.
 */
@Component
public class ApplicationConvertor implements ConvertorI<ApplicationE, ApplicationDO, ApplicationReqDTO> {


    @Override
    public ApplicationE doToEntity(ApplicationDO applicationDO) {
        ApplicationE applicationE = ApplicationFactory.createApplicationE();
        BeanUtils.copyProperties(applicationDO, applicationE);
        applicationE.initProjectE(applicationDO.getProjectId());
        applicationE.initGitlabProjectE(applicationDO.getGitlabProjectId());
        if (applicationDO.getAppTemplateId() != null) {
            applicationE.initApplicationTemplateE(applicationDO.getAppTemplateId());
        }
        return applicationE;
    }

    @Override
    public ApplicationDO entityToDo(ApplicationE applicationE) {
        ApplicationDO applicationDO = new ApplicationDO();
        BeanUtils.copyProperties(applicationE, applicationDO);
        applicationDO.setProjectId(applicationE.getProjectE().getId());
        if (applicationE.getApplicationTemplateE() != null) {
            applicationDO.setAppTemplateId(applicationE.getApplicationTemplateE().getId());
        }
        if (applicationE.getGitlabProjectE() != null) {
            applicationDO.setGitlabProjectId(applicationE.getGitlabProjectE().getId());
        }
        return applicationDO;
    }


    @Override
    public ApplicationE dtoToEntity(ApplicationReqDTO applicationReqDTO) {
        ApplicationE applicationE = ApplicationFactory.createApplicationE();
        BeanUtils.copyProperties(applicationReqDTO, applicationE);
        applicationE.initProjectE(applicationReqDTO.getProjectId());
        if (applicationReqDTO.getApplicationTemplateId() != null) {
            applicationE.initApplicationTemplateE(applicationReqDTO.getApplicationTemplateId());
        }
        return applicationE;
    }

    @Override
    public ApplicationReqDTO entityToDto(ApplicationE applicationE) {
        ApplicationReqDTO applicationReqDTO = new ApplicationReqDTO();
        BeanUtils.copyProperties(applicationE, applicationReqDTO);
        if (applicationE.getProjectE() != null) {
            applicationReqDTO.setProjectId(applicationE.getProjectE().getId());
        }
        if (applicationE.getApplicationTemplateE() != null) {
            applicationReqDTO.setApplicationTemplateId(applicationE.getApplicationTemplateE().getId());
        }
        return applicationReqDTO;
    }
    
}
