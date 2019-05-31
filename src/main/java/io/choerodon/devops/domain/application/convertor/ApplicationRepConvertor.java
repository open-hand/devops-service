package io.choerodon.devops.domain.application.convertor;


import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.dto.ApplicationRepDTO;
import io.choerodon.devops.domain.application.entity.ApplicationE;
import io.choerodon.devops.infra.common.util.TypeUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

@Component
public class ApplicationRepConvertor implements ConvertorI<ApplicationE, Object, ApplicationRepDTO> {

    @Override
    public ApplicationRepDTO entityToDto(ApplicationE applicationE) {
        ApplicationRepDTO applicationRepDTO = new ApplicationRepDTO();
        BeanUtils.copyProperties(applicationE, applicationRepDTO);
        if (applicationE.getApplicationTemplateE() != null) {
            applicationRepDTO.setApplicationTemplateId(applicationE.getApplicationTemplateE().getId());
        }
        if (applicationE.getIsSkipCheckPermission()) {
            applicationRepDTO.setPermission(true);
        } else {
            applicationRepDTO.setPermission(false);
        }
        if (applicationE.getHarborConfigE() != null) {
            applicationRepDTO.setHarborConfigId(applicationE.getHarborConfigE().getId());
        }
        if (applicationE.getChartConfigE() != null) {
            applicationRepDTO.setChartConfigId(applicationE.getChartConfigE().getId());
        }
        if (applicationE.getGitlabProjectE() != null) {
            applicationRepDTO.setGitlabProjectId(TypeUtil.objToLong(applicationE.getGitlabProjectE().getId()));
            applicationRepDTO.setRepoUrl(applicationE.getGitlabProjectE().getRepoURL());
        }
        applicationRepDTO.setFail(applicationE.getFailed());
        applicationRepDTO.setProjectId(applicationE.getProjectE().getId());
        return applicationRepDTO;
    }
}