package io.choerodon.devops.app.service;

import java.util.Date;
import java.util.List;

import com.github.pagehelper.PageInfo;

import io.choerodon.base.domain.PageRequest;
import io.choerodon.devops.api.vo.*;
import io.choerodon.devops.app.eventhandler.payload.DevOpsAppImportPayload;
import io.choerodon.devops.app.eventhandler.payload.DevOpsAppPayload;
import io.choerodon.devops.app.eventhandler.payload.IamAppPayLoad;
import io.choerodon.devops.infra.dto.ApplicationServiceDTO;
import io.choerodon.devops.infra.enums.GitPlatformType;

/**
 * Created by younger on 2018/3/28.
 */
public interface ApplicationSevriceService {

    /**
     * 项目下创建应用
     *
     * @param projectId         项目Id
     * @param applicationReqDTO 应用信息
     * @return ApplicationTemplateDTO
     */
    ApplicationServiceRepVO create(Long projectId, ApplicationServiceReqVO applicationReqDTO);


    /**
     * 项目下查询单个应用信息
     *
     * @param projectId     项目id
     * @param applicationId 应用Id
     * @return ApplicationRepDTO
     */
    ApplicationServiceRepVO query(Long projectId, Long applicationId);

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
    Boolean update(Long projectId, ApplicationUpdateVO applicationUpdateDTO);


    /**
     * 项目下启用停用应用信息
     *
     * @param applicationId 应用id
     * @param active        启用停用
     * @return Boolean
     */
    Boolean updateActive(Long applicationId, Boolean active);

    /**
     * 组织下分页查询应用
     *
     * @param projectId   项目id
     * @param isActive    是否启用
     * @param appMarket   应用市场导入
     * @param hasVersion  是否存在版本
     * @param pageRequest 分页参数
     * @param params      参数
     * @return Page
     */
    PageInfo<ApplicationServiceRepVO> pageByOptions(Long projectId,
                                                    Boolean isActive,
                                                    Boolean hasVersion,
                                                    Boolean appMarket,
                                                    String type,
                                                    Boolean doPage,
                                                    PageRequest pageRequest,
                                                    String params);


    PageInfo<ApplicationServiceRepVO> pageByOptionsAppMarket(Long projectId, Boolean isActive, Boolean hasVersion,
                                                             Boolean appMarket,
                                                             String type, Boolean doPage,
                                                             PageRequest pageRequest, String params);

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
     * @return baseList of ApplicationRepDTO
     */
    List<ApplicationServiceCodeVO> listByEnvId(Long projectId, Long envId, String status, Long appId);

    /**
     * 根据环境id获取已部署正在运行实例的应用
     *
     * @param projectId   项目id
     * @param envId       环境Id
     * @param pageRequest 分页参数
     * @return baseList of ApplicationRepDTO
     */
    PageInfo<ApplicationServiceCodeVO> pageByIds(Long projectId, Long envId, Long appId, PageRequest pageRequest);

    /**
     * 项目下查询所有已经启用的应用
     *
     * @param projectId 项目id
     * @return baseList of ApplicationRepDTO
     */
    List<ApplicationServiceRepVO> listByActive(Long projectId);

    /**
     * 项目下查询所有可选已经启用的应用
     *
     * @param projectId 项目id
     * @return baseList of ApplicationRepDTO
     */
    List<ApplicationServiceRepVO> listAll(Long projectId);

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
    List<ApplicationTemplateRespVO> listTemplate(Long projectId, Boolean isPredefined);

    /**
     * 项目下查询已经启用有版本未发布的应用
     *
     * @param projectId   项目id
     * @param pageRequest 分页参数
     * @param params      查询参数
     * @return baseList of ApplicationRepDTO
     */
    PageInfo<ApplicationServiceReqVO> pageByActiveAndPubAndVersion(Long projectId, PageRequest pageRequest, String params);

    /**
     * 项目下分页查询代码仓库
     *
     * @param projectId   项目id
     * @param pageRequest 分页参数
     * @param params      查询参数
     * @return page of ApplicationRepDTO
     */
    PageInfo<ApplicationServiceRepVO> pageCodeRepository(Long projectId, PageRequest pageRequest, String params);

    /**
     * 获取应用下所有用户权限
     *
     * @param appId 应用id
     * @return List
     */
    List<AppServiceUserPermissionRespVO> listAllUserPermission(Long appId);

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
     * @param projectId           project id
     * @param applicationImportVO 导入操作的相关信息
     * @return response
     */
    ApplicationServiceRepVO importApp(Long projectId, ApplicationImportVO applicationImportVO);


    /**
     * 根据应用code查询应用
     *
     * @param projectId 项目Id
     * @param code      应用code
     * @return ApplicationRepDTO
     */
    ApplicationServiceRepVO queryByCode(Long projectId, String code);


    /**
     * 处理iam服务创建应用
     *
     * @param devOpsAppPayload 应用相关信息
     */
    void createGitlabProject(Long projectId, DevOpsAppPayload devOpsAppPayload);


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
    Boolean checkHarbor(String url, String userName, String password, String project, String email);

    /**
     * 校验chart配置信息是否正确
     *
     * @param url chartmusume地址
     * @return Boolean
     */
    Boolean checkChart(String url);

    /**
     * 查看sonarqube相关信息
     *
     * @param projectId 项目Id
     * @param appId     应用id
     * @return
     */
    SonarContentsVO getSonarContent(Long projectId, Long appId);

    /**
     * 查看sonarqube相关报表
     *
     * @param projectId 项目Id
     * @param appId     应用id
     * @return
     */
    SonarTableVO getSonarTable(Long projectId, Long appId, String type, Date startTime, Date endTime);


    /**
     * 或者gitlab地址
     *
     * @param projectId
     * @param appId
     * @return
     */
    String getGitlabUrl(Long projectId, Long appId);

    /**
     * 获取远程应用
     *
     * @param projectId
     * @param pageRequest
     * @param params
     * @return
     */
    PageInfo<RemoteApplicationServiceVO> pageRemoteApps(Long projectId, PageRequest pageRequest, String params);

    /**
     * 获取共享应用
     *
     * @param projectId
     * @param pageRequest
     * @param params
     * @return
     */
    PageInfo<ApplicationServiceRepVO> pageShareApps(Long projectId, PageRequest pageRequest, String params);

    /**
     * 根据appServiceId查询应用服务有权限的项目成员和项目所有者
     *
     * @param projectId
     * @param appServiceId
     * @param pageRequest
     * @param searchParam
     */
    PageInfo<DevopsUserPermissionVO> pagePermissionUsers(Long projectId, Long appServiceId, PageRequest pageRequest, String searchParam);

    /**
     * 根据appServiceId查询应用服务所有没有权限的项目成员
     *
     * @param projectId
     * @param appServiceId
     * @param params
     * @return
     */
    List<DevopsUserPermissionVO> listMembers(Long projectId, Long appServiceId, String params);

    /**
     * 更新应用服务权限
     *
     * @param appServiceId
     * @param applicationPermissionVO
     */
    void updatePermission(Long projectId, Long appServiceId, ApplicationPermissionVO applicationPermissionVO);

    /**
     * 删除用户应用服务权限
     *
     * @param appServiceId
     * @param userId
     */
    void deletePermission(Long projectId, Long appServiceId, Long userId);

    void baseCheckApp(Long projectId, Long appId);

    int baseUpdate(ApplicationServiceDTO applicationDTO);

    void updateApplicationStatus(ApplicationServiceDTO applicationDTO);

    ApplicationServiceDTO baseQuery(Long applicationId);

    PageInfo<ApplicationServiceDTO> basePageByOptions(Long projectId, Boolean isActive, Boolean hasVersion, Boolean
            appMarket,
                                                      String type, Boolean doPage, PageRequest pageRequest, String params);

    PageInfo<ApplicationServiceDTO> basePageCodeRepository(Long projectId, PageRequest pageRequest, String params,
                                                           Boolean isProjectOwner, Long userId);


    ApplicationServiceDTO baseQueryByCode(String code, Long projectId);

    ApplicationServiceDTO baseQueryByCodeWithNullProject(String code);

    List<ApplicationServiceDTO> baseListByEnvId(Long projectId, Long envId, String status);

    PageInfo<ApplicationServiceDTO> basePageByEnvId(Long projectId, Long envId, Long appId, PageRequest pageRequest);

    List<ApplicationServiceDTO> baseListByActive(Long projectId);

    List<ApplicationServiceDTO> baseListDeployedApp(Long projectId);

    PageInfo<ApplicationServiceDTO> basePageByActiveAndPubAndHasVersion(Long projectId, Boolean isActive,
                                                                        PageRequest pageRequest, String params);

    ApplicationServiceDTO baseQueryByToken(String token);

    void baseCheckAppCanDisable(Long applicationId);

    List<ApplicationServiceDTO> baseListByCode(String code);

    List<ApplicationServiceDTO> baseListByGitLabProjectIds(List<Long> gitLabProjectIds);

    void baseDelete(Long appId);

    List<ApplicationServiceDTO> baseListByProjectIdAndSkipCheck(Long projectId);

    List<ApplicationServiceDTO> baseListByProjectId(Long projectId);

    void baseUpdateHarborConfig(Long projectId, Long newConfigId, Long oldConfigId, boolean harborPrivate);

    ApplicationServiceDTO getApplicationDTO(Long projectId, ApplicationServiceReqVO applicationReqDTO);

    void baseCheckName(Long projectId, String appName);

    void baseCheckCode(ApplicationServiceDTO applicationDTO);

    ApplicationServiceDTO baseCreate(ApplicationServiceDTO applicationDTO);
}
