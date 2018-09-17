package io.choerodon.devops.domain.application.convertor;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.dto.ApplicationTemplateDTO;
import io.choerodon.devops.domain.application.entity.ApplicationTemplateE;
import io.choerodon.devops.domain.application.factory.ApplicationTemplateFactory;
import io.choerodon.devops.infra.common.util.TypeUtil;
import io.choerodon.devops.infra.dataobject.ApplicationTemplateDO;

/**
 * Created by younger on 2018/3/27.
 */
@Component
public class ApplicationTemplateConvertor implements ConvertorI<ApplicationTemplateE, ApplicationTemplateDO, ApplicationTemplateDTO> {

    @Override
    public ApplicationTemplateE doToEntity(ApplicationTemplateDO applicationTemplateDO) {
        ApplicationTemplateE applicationTemplateE = ApplicationTemplateFactory.createApplicationTemplateE();
        BeanUtils.copyProperties(applicationTemplateDO, applicationTemplateE);
        applicationTemplateE.initOrganization(applicationTemplateDO.getOrganizationId());
        if (applicationTemplateDO.getGitlabProjectId() != null) {
            applicationTemplateE.initGitlabProjectE(TypeUtil.objToInteger(applicationTemplateDO.getGitlabProjectId()));

        }
        return applicationTemplateE;
    }

    @Override
    public ApplicationTemplateDO entityToDo(ApplicationTemplateE applicationTemplateE) {
        ApplicationTemplateDO applicationTemplateDO = new ApplicationTemplateDO();
        BeanUtils.copyProperties(applicationTemplateE, applicationTemplateDO);
        applicationTemplateDO.setOrganizationId(applicationTemplateE.getOrganization().getId());
        if (applicationTemplateE.getGitlabProjectE() != null) {
            applicationTemplateDO.setGitlabProjectId(
                    TypeUtil.objToLong(applicationTemplateE.getGitlabProjectE().getId()));
        }
        return applicationTemplateDO;
    }


    @Override
    public ApplicationTemplateE dtoToEntity(ApplicationTemplateDTO applicationTemplateDTO) {
        ApplicationTemplateE applicationTemplateE = ApplicationTemplateFactory.createApplicationTemplateE();
        BeanUtils.copyProperties(applicationTemplateDTO, applicationTemplateE);
        applicationTemplateE.initOrganization(applicationTemplateDTO.getOrganizationId());
        return applicationTemplateE;
    }

}
