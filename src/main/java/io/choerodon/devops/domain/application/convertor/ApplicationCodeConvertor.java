package io.choerodon.devops.domain.application.convertor;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.devops.api.vo.ApplicationCodeDTO;
import io.choerodon.devops.domain.application.entity.ApplicationE;

/**
 * Created by Zenger on 2018/4/2.
 */
@Component
public class ApplicationCodeConvertor implements ConvertorI<ApplicationE, Object, ApplicationCodeDTO> {

    @Override
    public ApplicationCodeDTO entityToDto(ApplicationE entity) {
        ApplicationCodeDTO applicationCodeDTO = new ApplicationCodeDTO();
        BeanUtils.copyProperties(entity, applicationCodeDTO);
        applicationCodeDTO.setProjectId(entity.getProjectE().getId());
        return applicationCodeDTO;
    }
}
