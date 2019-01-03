package io.choerodon.devops.app.service;

import java.util.List;

import io.choerodon.asgard.saga.feign.SagaClient;
import io.choerodon.core.domain.Page;
import io.choerodon.devops.api.dto.*;
import io.choerodon.devops.domain.application.event.DevOpsAppPayload;
import io.choerodon.devops.infra.common.util.enums.GitPlatformType;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * Created by younger on 2018/3/28.
 */
public interface ApplicationService {

    /**
     * 项目下创建应用
     *
     * @param projectId         项目Id
     * @param applicationReqDTO 应用信息
     * @return ApplicationTemplateDTO
     */
    ApplicationRepDTO create(Long projectId, ApplicationReqDTO applicationReqDTO);


    /**
     * 项目下查询单个应用信息
     *
     * @param projectId     项目id
     * @param applicationId 应用Id
     * @return ApplicationRepDTO
     */
    ApplicationRepDTO query(Long projectId, Long applicationId);

    /**
     * 项目下删除创建失败应用
     *
     * @param projectId     项目id
     * @param applicationId 应用Id
     */
    void delete(Long projectId, Long applicationId);

    /**
     * 项目下更新应用信息
     *
     * @param projectId            项目id
     * @param applicationUpdateDTO 应用信息
     * @return Boolean
     */
    Boolean update(Long projectId, ApplicationUpdateDTO applicationUpdateDTO);


    /**
     * 项目下启用停用应用信息
     *
     * @param applicationId 应用id
     * @param active        启用停用
     * @return Boolean
     */
    Boolean active(Long applicationId, Boolean active);

    /**
     * 组织下分页查询应用
     *
     * @param projectId   项目id
     * @param isActive    是否启用
     * @param hasVersion  是否存在版本
     * @param pageRequest 分页参数
     * @param params      参数
     * @return Page
     */
    Page<ApplicationRepDTO> listByOptions(Long projectId,
                                          Boolean isActive,
                                          Boolean hasVersion,
                                          String type,
                                          Boolean doPage,
                                          PageRequest pageRequest,
                                          String params);

    /**
     * 处理应用创建逻辑
     *
     * @param gitlabProjectEventDTO 应用信息
     */
    void operationApplication(DevOpsAppPayload gitlabProjectEventDTO);


    /**
     * 设置应用创建失败状态
     *
     * @param gitlabProjectEventDTO 应用信息
     * @param projectId             可为空
     */
    void setAppErrStatus(String gitlabProjectEventDTO, Long projectId);

    Boolean applicationExist(String uuid);

    /**
     * 项目下应用查询ci脚本文件
     *
     * @param token token
     * @return File
     */
    String queryFile(String token, String type);

    /**
     * 根据环境id获取已部署正在运行实例的应用
     *
     * @param projectId 项目id
     * @param envId     环境Id
     * @param status    环境状态
     * @return list of ApplicationRepDTO
     */
    List<ApplicationCodeDTO> listByEnvId(Long projectId, Long envId, String status, Long appId);

    /**
     * 根据环境id获取已部署正在运行实例的应用
     *
     * @param projectId   项目id
     * @param envId       环境Id
     * @param pageRequest 分页参数
     * @return list of ApplicationRepDTO
     */
    Page<ApplicationCodeDTO> pageByEnvId(Long projectId, Long envId, PageRequest pageRequest);

    /**
     * 项目下查询所有已经启用的应用
     *
     * @param projectId 项目id
     * @return list of ApplicationRepDTO
     */
    List<ApplicationRepDTO> listByActive(Long projectId);

    /**
     * 项目下查询所有可选已经启用的应用
     *
     * @param projectId 项目id
     * @return list of ApplicationRepDTO
     */
    List<ApplicationRepDTO> listAll(Long projectId);

    /**
     * 创建应用校验名称是否存在
     *
     * @param projectId 项目id
     * @param name      应用name
     */
    void checkName(Long projectId, String name);

    /**
     * 创建应用校验编码是否存在
     *
     * @param projectId 项目ID
     * @param code      应用code
     */
    void checkCode(Long projectId, String code);

    /**
     * 查询应用模板
     *
     * @param projectId 项目ID
     * @return Page
     */
    List<ApplicationTemplateRepDTO> listTemplate(Long projectId);

    /**
     * 项目下查询已经启用有版本未发布的应用
     *
     * @param projectId   项目id
     * @param pageRequest 分页参数
     * @param params      查询参数
     * @return list of ApplicationRepDTO
     */
    Page<ApplicationReqDTO> listByActiveAndPubAndVersion(Long projectId, PageRequest pageRequest, String params);

    /**
     * 项目下分页查询代码仓库
     *
     * @param projectId   项目id
     * @param pageRequest 分页参数
     * @param params      查询参数
     * @return page of ApplicationRepDTO
     */
    Page<ApplicationRepDTO> listCodeRepository(Long projectId, PageRequest pageRequest, String params);

    /**
     * 获取应用下所有用户权限
     *
     * @param appId 应用id
     * @return List
     */
    List<AppUserPermissionRepDTO> listAllUserPermission(Long appId);

    void initMockService(SagaClient sagaClient);

    /**
     * valid the repository url and access token
     *
     * @param gitPlatformType git platform type
     * @param repositoryUrl repository url
     * @param access_token  access token (Nullable)
     * @return true if valid
     */
    boolean checkRepositoryUrlAndToken(GitPlatformType gitPlatformType, String repositoryUrl, String access_token);
}
