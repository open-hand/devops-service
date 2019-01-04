package io.choerodon.devops.domain.application.convertor;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.dto.ApplicationImportDTO;
import io.choerodon.devops.domain.application.entity.ApplicationE;
import io.choerodon.devops.domain.application.factory.ApplicationFactory;
import io.choerodon.devops.infra.dataobject.ApplicationDO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

/**
 * @author zmf
 */
@Component
public class ApplicationImportConvertor implements ConvertorI<ApplicationE, ApplicationDO, ApplicationImportDTO> {

    @Override
    public ApplicationE dtoToEntity(ApplicationImportDTO applicationImportDTO) {
        ApplicationE applicationE = ApplicationFactory.createApplicationE();
        applicationE.initProjectE(applicationImportDTO.getProjectId());
        BeanUtils.copyProperties(applicationImportDTO, applicationE);
        if (applicationImportDTO.getApplicationTemplateId() != null) {
            applicationE.initApplicationTemplateE(applicationImportDTO.getApplicationTemplateId());
        }
        return applicationE;
    }

    @Override
    public ApplicationImportDTO entityToDto(ApplicationE applicationE) {
        ApplicationImportDTO applicationImportDTO = new ApplicationImportDTO();
        BeanUtils.copyProperties(applicationE, applicationImportDTO);
        if (applicationE.getProjectE() != null) {
            applicationImportDTO.setProjectId(applicationE.getProjectE().getId());
        }
        if (applicationE.getApplicationTemplateE() != null) {
            applicationImportDTO.setApplicationTemplateId(applicationE.getApplicationTemplateE().getId());
        }
        return applicationImportDTO;
    }
}
