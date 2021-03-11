package io.choerodon.devops.app.service;

import io.choerodon.core.domain.Page;
import io.choerodon.devops.api.vo.DevopsAppTemplateCreateVO;
import io.choerodon.devops.infra.dto.DevopsAppTemplateDTO;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * @Author: scp
 * @Description:
 * @Date: Created in 2021/3/9
 * @Modified By:
 */
public interface DevopsAppTemplateService {
    Page<DevopsAppTemplateDTO> pageAppTemplate(Long sourceId, String sourceType, String params, PageRequest pageRequest);

    void createTemplate(Long sourceId, String sourceType, DevopsAppTemplateCreateVO appTemplateCreateVO);

    void createTemplateSagaTask(DevopsAppTemplateCreateVO appTemplateCreateVO);

    Boolean checkNameAndCode(DevopsAppTemplateDTO appTemplateDTO, String type);

    void modifyName(Long appTemplateId, String name, Long sourceId, String sourceType);

    void addPermission(Long appTemplateId);

    void enableAppTemplate(Long appTemplateId);

    void disableAppTemplate(Long appTemplateId);

    void deleteAppTemplate(Long appTemplateId);


}
