package io.choerodon.devops.app.assember;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.dto.ApplicationRepDTO;
import io.choerodon.devops.domain.application.entity.ApplicationE;

/**
 * Created by younger on 2018/3/30.
 */
@Component
public class ApplicaitonRepAssember implements ConvertorI<ApplicationE, Object, ApplicationRepDTO> {

    @Override
    public ApplicationRepDTO entityToDto(ApplicationE applicationE) {
        ApplicationRepDTO applicationRepDTO = new ApplicationRepDTO();
        BeanUtils.copyProperties(applicationE, applicationRepDTO);
        if (applicationE.getApplicationTemplateE() != null) {
            applicationRepDTO.setApplictionTemplateId(applicationE.getApplicationTemplateE().getId());
        }
        applicationRepDTO.setRepoUrl(applicationE.getGitlabProjectE().getRepoURL());
        applicationRepDTO.setProjectId(applicationE.getProjectE().getId());
        return applicationRepDTO;
    }
}
