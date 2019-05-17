package io.choerodon.devops.app.service;

import java.util.Date;
import java.util.List;

import io.choerodon.core.domain.Page;
import io.choerodon.devops.api.dto.*;
import io.choerodon.devops.api.dto.gitlab.VariableDTO;
import io.choerodon.devops.domain.application.event.DevOpsAppImportPayload;
import io.choerodon.devops.domain.application.event.DevOpsAppPayload;
import io.choerodon.devops.domain.application.event.IamAppPayLoad;
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
     * 处理应用导入逻辑
     *
     * @param devOpsAppImportPayload 应用导入相关信息
     */
    void operationApplicationImport(DevOpsAppImportPayload devOpsAppImportPayload);


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
    Page<ApplicationCodeDTO> pageByEnvId(Long projectId, Long envId, Long appId, PageRequest pageRequest);

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
     * @param projectId    项目ID
     * @param isPredefined 是否只查询预定义模板
     * @return Page
     */
    List<ApplicationTemplateRepDTO> listTemplate(Long projectId, Boolean isPredefined);

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

    /**
     * valid the repository url and access token
     *
     * @param gitPlatformType git platform type
     * @param repositoryUrl   repository url
     * @param accessToken     access token (Nullable)
     * @return true if valid
     */
    Boolean validateRepositoryUrlAndToken(GitPlatformType gitPlatformType, String repositoryUrl, String accessToken);

    /**
     * 从外部代码托管平台导入项目创建应用
     *
     * @param projectId            project id
     * @param applicationImportDTO 导入操作的相关信息
     * @return response
     */
    ApplicationRepDTO importApplicationFromGitPlatform(Long projectId, ApplicationImportDTO applicationImportDTO);


    /**
     * 根据应用code查询应用
     *
     * @param projectId 项目Id
     * @param code      应用code
     * @return ApplicationRepDTO
     */
    ApplicationRepDTO queryByCode(Long projectId, String code);


    /**
     * 处理iam服务创建应用
     *
     * @param iamAppPayLoad 应用相关信息
     */
    void createIamApplication(IamAppPayLoad iamAppPayLoad);


    /**
     * 处理iam服务更新应用
     *
     * @param iamAppPayLoad 应用相关信息
     */
    void updateIamApplication(IamAppPayLoad iamAppPayLoad);

    /**
     * 处理iam服务删除应用
     *
     * @param iamAppPayLoad 应用相关信息
     */
    void deleteIamApplication(IamAppPayLoad iamAppPayLoad);

    /**
     * 校验harbor配置信息是否正确
     *
     * @param url      harbor地址
     * @param userName harbor用户名
     * @param password harbor密码
     * @param project  harbor项目
     * @param email    harbor邮箱
     * @return Boolean
     */
    Boolean checkHarborIsUsable(String url, String userName, String password, String project, String email);

    /**
     * 校验chart配置信息是否正确
     *
     * @param url chartmusume地址
     * @return Boolean
     */
    Boolean checkChartIsUsable(String url);

    /**
     * 根据配置Id查询配置并转换成VariableDTO
     *
     * @param harborConfigId harbor配置Id
     * @param chartConfigId  chart配置Id
     * @return
     */
    List<VariableDTO> setVariableDTO(Long harborConfigId, Long chartConfigId);


    /**
     * 查看sonarqube相关信息
     *
     * @param projectId 项目Id
     * @param appId     应用id
     * @return
     */
    SonarContentsDTO getSonarContent(Long projectId, Long appId);

    /**
     * 查看sonarqube相关报表
     *
     * @param projectId 项目Id
     * @param appId     应用id
     * @return
     */
    SonarTableDTO getSonarTable(Long projectId, Long appId, String type, Date startTime, Date endTime);
}
