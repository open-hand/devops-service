package io.choerodon.devops.app.service;

import java.util.List;

import io.choerodon.core.domain.Page;
import io.choerodon.devops.api.dto.ApplicationTemplateDTO;
import io.choerodon.devops.api.dto.ApplicationTemplateRepDTO;
import io.choerodon.devops.api.dto.ApplicationTemplateUpdateDTO;
import io.choerodon.devops.api.dto.GitlabProjectEventDTO;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * Created by younger on 2018/3/27.
 */
public interface ApplicationTemplateService {

    /**
     * 组织下创建应用模板
     *
     * @param applicationTemplateDTO 模板信息
     * @return ApplicationTemplateDTO
     */
    ApplicationTemplateRepDTO create(ApplicationTemplateDTO applicationTemplateDTO, Long organizationId);

    /**
     * 组织下更新应用模板
     *
     * @param applicationTemplateUpdateDTO 模板信息
     * @return ApplicationTemplateDTO
     */
    ApplicationTemplateRepDTO update(ApplicationTemplateUpdateDTO applicationTemplateUpdateDTO, Long organizationId);

    /**
     * 组织下删除应用模板
     *
     * @param appTemplateId 模板id
     */
    void delete(Long appTemplateId);

    /**
     * 组织下查询单个应用模板
     *
     * @param appTemplateId 模板id
     * @return ApplicationTemplateDTO
     */
    ApplicationTemplateRepDTO query(Long appTemplateId);

    /**
     * 组织下分页查询应用模板
     *
     * @param pageRequest 分页参数
     * @return Page
     */
    Page<ApplicationTemplateRepDTO> listByOptions(PageRequest pageRequest, Long organizaitionId, String searchParam);

    /**
     * 处理模板创建逻辑
     *
     * @param gitlabProjectEventDTO 模板信息
     */
    void operationApplicationTemplate(GitlabProjectEventDTO gitlabProjectEventDTO);

    /**
     * 组织下查询应用模板
     *
     * @return List
     */
    List<ApplicationTemplateRepDTO> list(Long organizationId);

    /**
     * 创建模板校验名称是否存在
     *
     * @param organizationId 组织id
     * @param name           模板name
     * @return
     */
    void checkName(Long organizationId, String name);

    /**
     * 创建模板校验编码是否存在
     *
     * @param organizationId 组织id
     * @param code           模板code
     * @return
     */
    void checkCode(Long organizationId, String code);

    /**
     * 判断模板是否存在
     *
     * @param uuid 模板uuid
     * @return
     */
    Boolean applicationTemplateExist(String uuid);
}
