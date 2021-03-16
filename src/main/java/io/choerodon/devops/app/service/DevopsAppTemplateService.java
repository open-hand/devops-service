package io.choerodon.devops.app.service;

import java.util.List;

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

    List<DevopsAppTemplateDTO> listAppTemplate(Long sourceId, String sourceType, String selectedLevel, String param);

    void createTemplate(Long sourceId, String sourceType, DevopsAppTemplateCreateVO appTemplateCreateVO);

    void createTemplateSagaTask(DevopsAppTemplateCreateVO appTemplateCreateVO);

    Boolean checkNameAndCode(DevopsAppTemplateDTO appTemplateDTO, String type);

    void modifyName(Long appTemplateId, String name, Long sourceId, String sourceType);

    void addPermission(Long appTemplateId);

    void enableAppTemplate(Long appTemplateId);

    void disableAppTemplate(Long appTemplateId);

    void deleteAppTemplate(Long sourceId, String sourceType, Long appTemplateId);

    void deleteAppTemplateSagaTask(Long gitlabProjectId);

    String getTemplateGroupPath(Long appTemplateId);

    DevopsAppTemplateDTO queryAppTemplateById(Long appTemplateId);

    void updateAppTemplateStatus(Long appTemplateId);

    void updateAppTemplate(Long appTemplate, String name);

}
