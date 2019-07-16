package io.choerodon.devops.infra.convertor;

import io.choerodon.devops.api.vo.ApplicationTemplateVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.vo.iam.entity.ApplicationTemplateE;
import io.choerodon.devops.domain.application.factory.ApplicationTemplateFactory;
import io.choerodon.devops.infra.util.TypeUtil;
import io.choerodon.devops.infra.dto.ApplicationTemplateDTO;

/**
 * Created by younger on 2018/3/27.
 */
@Component
public class ApplicationTemplateConvertor implements ConvertorI<ApplicationTemplateE, ApplicationTemplateDTO, ApplicationTemplateVO> {

    @Override
    public ApplicationTemplateE doToEntity(ApplicationTemplateDTO applicationTemplateDTO) {
        ApplicationTemplateE applicationTemplateE = ApplicationTemplateFactory.createApplicationTemplateE();
        BeanUtils.copyProperties(applicationTemplateDTO, applicationTemplateE);
        applicationTemplateE.initOrganization(applicationTemplateDTO.getOrganizationId());
        if (applicationTemplateDTO.getGitlabProjectId() != null) {
            applicationTemplateE.initGitlabProjectE(TypeUtil.objToInteger(applicationTemplateDTO.getGitlabProjectId()));

        }
        return applicationTemplateE;
    }

    @Override
    public ApplicationTemplateDTO entityToDo(ApplicationTemplateE applicationTemplateE) {
        ApplicationTemplateDTO applicationTemplateDTO = new ApplicationTemplateDTO();
        BeanUtils.copyProperties(applicationTemplateE, applicationTemplateDTO);
        applicationTemplateDTO.setOrganizationId(applicationTemplateE.getOrganization().getId());
        if (applicationTemplateE.getGitlabProjectE() != null) {
            applicationTemplateDTO.setGitlabProjectId(
                    TypeUtil.objToLong(applicationTemplateE.getGitlabProjectE().getId()));
        }
        return applicationTemplateDTO;
    }


    @Override
    public ApplicationTemplateE dtoToEntity(ApplicationTemplateVO applicationTemplateVO) {
        ApplicationTemplateE applicationTemplateE = ApplicationTemplateFactory.createApplicationTemplateE();
        BeanUtils.copyProperties(applicationTemplateVO, applicationTemplateE);
        applicationTemplateE.initOrganization(applicationTemplateVO.getOrganizationId());
        return applicationTemplateE;
    }

}
