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

    void createTemplateOnSite(Long sourceId, String sourceType, DevopsAppTemplateCreateVO appTemplateCreateVO);

    Boolean checkNameAndCode(DevopsAppTemplateDTO appTemplateDTO);

}
