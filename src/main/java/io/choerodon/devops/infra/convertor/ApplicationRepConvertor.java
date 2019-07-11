package io.choerodon.devops.infra.convertor;


import io.choerodon.core.convertor.ConvertorI;
<<<<<<< HEAD
=======
<<<<<<< HEAD
import io.choerodon.devops.api.vo.ApplicationRepVO;
>>>>>>> [IMP] applicationController重构
import io.choerodon.devops.domain.application.entity.ApplicationE;
=======
>>>>>>> 99504a39d606d3005354e0b1bdcb50530cde6afd
import io.choerodon.devops.api.vo.iam.entity.ApplicationE;
<<<<<<< HEAD:src/main/java/io/choerodon/devops/infra/convertor/ApplicationRepConvertor.java
>>>>>>> [IMP] 修改AppControler重构
=======
>>>>>>> [IMP] 修改AppControler重构:src/main/java/io/choerodon/devops/domain/application/convertor/ApplicationRepConvertor.java
import io.choerodon.devops.infra.util.TypeUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

@Component
public class ApplicationRepConvertor implements ConvertorI<ApplicationE, Object, ApplicationRepVO> {

    @Override
    public ApplicationRepVO entityToDto(ApplicationE applicationE) {
        ApplicationRepVO applicationRepDTO = new ApplicationRepVO();
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