package io.choerodon.devops.infra.convertor;

import io.choerodon.core.convertor.ConvertorI;
<<<<<<< HEAD:src/main/java/io/choerodon/devops/domain/application/convertor/ApplicationConvertor.java
<<<<<<< HEAD
=======
<<<<<<< HEAD
import io.choerodon.devops.api.vo.ApplicationReqVO;
>>>>>>> [IMP] applicationController重构
import io.choerodon.devops.domain.application.entity.ApplicationE;
=======
>>>>>>> 99504a39d606d3005354e0b1bdcb50530cde6afd
import io.choerodon.devops.api.vo.iam.entity.ApplicationE;
<<<<<<< HEAD
<<<<<<< HEAD:src/main/java/io/choerodon/devops/infra/convertor/ApplicationConvertor.java
>>>>>>> [IMP] 修改AppControler重构
import io.choerodon.devops.domain.application.factory.ApplicationFactory;
=======
import io.choerodon.devops.api.vo.ApplicationReqVO;
import io.choerodon.devops.api.vo.iam.entity.ApplicationE;
>>>>>>> [IMP]修复后端结构:src/main/java/io/choerodon/devops/infra/convertor/ApplicationConvertor.java
import io.choerodon.devops.infra.dataobject.ApplicationDTO;
import io.choerodon.devops.infra.dto.ApplicationDO;
=======
import io.choerodon.devops.domain.application.factory.ApplicationFactory;
import io.choerodon.devops.infra.dataobject.ApplicationDTO;
>>>>>>> [IMP] 修改AppControler重构:src/main/java/io/choerodon/devops/domain/application/convertor/ApplicationConvertor.java
=======
import io.choerodon.devops.infra.dto.ApplicationDTO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;


>>>>>>> [IMP]重构后端代码

/**
 * Created by Zenger on 2018/4/2.
 */
@Component
public class ApplicationConvertor implements ConvertorI<ApplicationE, ApplicationDTO, ApplicationReqVO> {


    @Override
    public ApplicationE doToEntity(ApplicationDTO applicationDO) {
        ApplicationE applicationE = ApplicationFactory.createApplicationE();
        BeanUtils.copyProperties(applicationDO, applicationE);
        applicationE.initProjectE(applicationDO.getProjectId());
        if (applicationDO.getGitlabProjectId() != null) {
            applicationE.initGitlabProjectE(applicationDO.getGitlabProjectId());
        }
        if (applicationDO.getAppTemplateId() != null) {
            applicationE.initApplicationTemplateE(applicationDO.getAppTemplateId());
        }
        if (applicationDO.getHarborConfigId() != null) {
            applicationE.initHarborConfig(applicationDO.getHarborConfigId());
        }
        if (applicationDO.getChartConfigId() != null) {
            applicationE.initChartConfig(applicationDO.getChartConfigId());
        }
        return applicationE;
    }

    @Override
    public ApplicationDTO entityToDo(ApplicationE applicationE) {
        ApplicationDTO applicationDO = new ApplicationDTO();
        BeanUtils.copyProperties(applicationE, applicationDO);
        if (applicationE.getProjectE() != null) {
            applicationDO.setProjectId(applicationE.getProjectE().getId());
        }
        if (applicationE.getApplicationTemplateE() != null) {
            applicationDO.setAppTemplateId(applicationE.getApplicationTemplateE().getId());
        }
        if (applicationE.getGitlabProjectE() != null) {
            applicationDO.setGitlabProjectId(applicationE.getGitlabProjectE().getId());
        }
        if (applicationE.getHarborConfigE() != null) {
            applicationDO.setHarborConfigId(applicationE.getHarborConfigE().getId());
        }
        if (applicationE.getChartConfigE() != null) {
            applicationDO.setChartConfigId(applicationE.getChartConfigE().getId());
        }
        return applicationDO;
    }


    @Override
    public ApplicationE dtoToEntity(ApplicationReqVO applicationReqDTO) {
        ApplicationE applicationE = ApplicationFactory.createApplicationE();
        BeanUtils.copyProperties(applicationReqDTO, applicationE);
        if (applicationReqDTO.getProjectId() != null) {
            applicationE.initProjectE(applicationReqDTO.getProjectId());
        }
        if (applicationReqDTO.getApplicationTemplateId() != null) {
            applicationE.initApplicationTemplateE(applicationReqDTO.getApplicationTemplateId());
        }
        if (applicationReqDTO.getHarborConfigId() != null) {
            applicationE.initHarborConfig(applicationReqDTO.getHarborConfigId());
        }
        if (applicationReqDTO.getChartConfigId() != null) {
            applicationE.initChartConfig(applicationReqDTO.getChartConfigId());
        }
        return applicationE;
    }

    @Override
    public ApplicationReqVO entityToDto(ApplicationE applicationE) {
        ApplicationReqVO applicationReqDTO = new ApplicationReqVO();
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
