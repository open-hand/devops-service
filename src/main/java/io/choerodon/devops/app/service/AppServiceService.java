package io.choerodon.devops.app.service;

import java.util.Date;
import java.util.List;

import com.github.pagehelper.PageInfo;

import io.choerodon.base.domain.PageRequest;
import io.choerodon.devops.api.vo.*;
import io.choerodon.devops.app.eventhandler.payload.AppServiceImportPayload;
import io.choerodon.devops.app.eventhandler.payload.DevOpsAppImportServicePayload;
import io.choerodon.devops.app.eventhandler.payload.DevOpsAppServicePayload;
import io.choerodon.devops.infra.dto.AppServiceDTO;
import io.choerodon.devops.infra.dto.UserAttrDTO;
import io.choerodon.devops.infra.enums.GitPlatformType;

/**
 * Created by younger on 2018/3/28.
 */
public interface AppServiceService {

    /**
     * 项目下创建服务
     *
     * @param projectId         项目Id
     * @param applicationReqDTO 服务信息
     * @return ApplicationTemplateDTO
     */
    AppServiceRepVO create(Long projectId, AppServiceReqVO applicationReqDTO);


    /**
     * 项目下查询单个服务信息
     *
     * @param projectId     项目id
     * @param applicationId 服务Id
     * @return ApplicationRepDTO
     */
    AppServiceRepVO query(Long projectId, Long applicationId);

    /**
     * 项目下删除创建失败服务
     *
     * @param projectId     项目id
     * @param applicationId 服务Id
     */
    void delete(Long projectId, Long applicationId);

    /**
     * 项目下更新服务信息
     *
     * @param projectId            项目id
     * @param applicationUpdateDTO 服务信息
     * @return Boolean
     */
    Boolean update(Long projectId, AppServiceUpdateDTO applicationUpdateDTO);


    /**
     * 项目下启用停用服务信息
     *
     * @param projectId     项目id
     * @param applicationId 服务id
     * @param active        启用停用
     * @return Boolean
     */
    Boolean updateActive(Long projectId, Long applicationId, Boolean active);

    /**
     * 组织下分页查询服务
     *
     * @param projectId   项目id
     * @param isActive    是否启用
     * @param appMarket   服务市场导入
     * @param hasVersion  是否存在版本
     * @param pageRequest 分页参数
     * @param params      参数
     * @return Page
     */
    PageInfo<AppServiceRepVO> pageByOptions(Long projectId,
                                            Boolean isActive,
                                            Boolean hasVersion,
                                            Boolean appMarket,
                                            String type,
                                            Boolean doPage,
                                            PageRequest pageRequest,
                                            String params);

    /**
     * 处理服务创建逻辑
     *
     * @param gitlabProjectEventDTO 服务信息
     */
    void operationApplication(DevOpsAppServicePayload gitlabProjectEventDTO);

    /**
     * 处理服务导入逻辑
     *
     * @param devOpsAppImportPayload 服务导入相关信息
     */
    void operationAppServiceImport(DevOpsAppImportServicePayload devOpsAppImportPayload);


    /**
     * 设置服务创建失败状态
     *
     * @param gitlabProjectEventDTO 服务信息
     * @param projectId             可为空
     */
    void setAppErrStatus(String gitlabProjectEventDTO, Long projectId);

    /**
     * 项目下服务查询ci脚本文件
     *
     * @param token token
     * @return File
     */
    String queryFile(String token, String type);

    /**
     * 根据环境id获取已部署正在运行实例的服务
     *
     * @param projectId 项目id
     * @param envId     环境Id
     * @param status    环境状态
     * @return baseList of ApplicationRepDTO
     */
    List<AppServiceCodeVO> listByEnvId(Long projectId, Long envId, String status, Long appServiceId);

    /**
     * 根据环境id获取已部署正在运行实例的服务
     *
     * @param projectId   项目id
     * @param envId       环境Id
     * @param pageRequest 分页参数
     * @return baseList of ApplicationRepDTO
     */
    PageInfo<AppServiceCodeVO> pageByIds(Long projectId, Long envId, Long appServiceId, PageRequest pageRequest);

    /**
     * 项目下查询所有已经启用的服务
     *
     * @param projectId 项目id
     * @return baseList of ApplicationRepDTO
     */
    List<AppServiceRepVO> listByActive(Long projectId);

    /**
     * 项目下查询所有可选已经启用的服务
     *
     * @param projectId 项目id
     * @return baseList of ApplicationRepDTO
     */
    List<AppServiceRepVO> listAll(Long projectId);

    /**
     * 创建服务校验名称是否存在
     *
     * @param projectId 项目Id
     * @param name      服务name
     */
    void checkName(Long projectId, String name);

    /**
     * 创建服务校验编码是否存在
     *
     * @param projectId 项目id
     * @param code      服务code
     */
    void checkCode(Long projectId, String code);

    /**
     * 批量校验应用服务code和name
     *
     * @param projectId
     * @param appServiceBatchCheckVO
     * @return
     */
    AppServiceBatchCheckVO checkCodeByProjectId(Long projectId, AppServiceBatchCheckVO appServiceBatchCheckVO);

    /**
     * 创建服务校验名称是否存在
     *
     * @param projectId 项目ID
     * @param name      服务name
     */
    void checkNameByProjectId(Long projectId, String name);

    /**
     * 创建服务校验编码是否存在
     *
     * @param projectId 项目ID
     * @param code      服务code
     */
    void checkCodeByProjectId(Long projectId, String code);

    /**
     * 项目下查询已经启用有版本未发布的服务
     *
     * @param projectId   项目id
     * @param pageRequest 分页参数
     * @param params      查询参数
     * @return baseList of ApplicationRepDTO
     */
    PageInfo<AppServiceReqVO> pageByActiveAndPubAndVersion(Long projectId, PageRequest pageRequest, String params);

    /**
     * 项目下分页查询代码仓库
     *
     * @param projectId   项目id
     * @param pageRequest 分页参数
     * @param params      查询参数
     * @return page of ApplicationRepDTO
     */
    PageInfo<AppServiceRepVO> pageCodeRepository(Long projectId, PageRequest pageRequest, String params);

    /**
     * 获取服务下所有用户权限
     *
     * @param appServiceId 服务id
     * @return List
     */
    List<AppServiceUserPermissionRespVO> listAllUserPermission(Long appServiceId);

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
     * 从外部代码托管平台导入项目创建服务
     *
     * @param projectId          project id
     * @param appServiceImportVO 导入操作的相关信息
     * @return response
     */
    AppServiceRepVO importApp(Long projectId, AppServiceImportVO appServiceImportVO);

    /**
     * 发送创建应用服务消息
     *
     * @param appServiceDTO 应用服务信息
     * @param projectId     项目id
     */
    void sendCreateAppServiceInfo(AppServiceDTO appServiceDTO, Long projectId);

    /**
     * 根据服务code查询服务
     *
     * @param projectId 项目Id
     * @param code      服务code
     * @return ApplicationRepDTO
     */
    AppServiceRepVO queryByCode(Long projectId, String code);


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
     * @param projectId    项目Id
     * @param appServiceId 服务id
     * @return
     */
    SonarContentsVO getSonarContent(Long projectId, Long appServiceId);

    /**
     * 查看sonarqube相关报表
     *
     * @param projectId    项目Id
     * @param appServiceId 服务id
     * @return
     */
    SonarTableVO getSonarTable(Long projectId, Long appServiceId, String type, Date startTime, Date endTime);


    /**
     * 或者gitlab地址
     *
     * @param projectId
     * @param appServiceId
     * @return
     */
    String getGitlabUrl(Long projectId, Long appServiceId);

    /**
     * 获取共享服务
     *
     * @param projectId
     * @param pageRequest
     * @param params
     * @return
     */
    PageInfo<AppServiceRepVO> pageShareAppService(Long projectId, PageRequest pageRequest, String params);

    /**
     * 根据appServiceId查询服务服务有权限的项目成员和项目所有者
     *
     * @param projectId
     * @param appServiceId
     * @param pageRequest
     * @param searchParam
     */
    PageInfo<DevopsUserPermissionVO> pagePermissionUsers(Long projectId, Long appServiceId, PageRequest pageRequest, String searchParam);

    /**
     * 根据appServiceId查询服务服务所有没有权限的项目成员
     *
     * @param projectId
     * @param appServiceId
     * @param params
     * @return
     */
    List<DevopsUserPermissionVO> listMembers(Long projectId, Long appServiceId, String params);

    /**
     * 更新服务服务权限
     *
     * @param appServiceId
     * @param applicationPermissionVO
     */
    void updatePermission(Long projectId, Long appServiceId, AppServicePermissionVO applicationPermissionVO);

    /**
     * 删除用户服务服务权限
     *
     * @param appServiceId
     * @param userId
     */
    void deletePermission(Long projectId, Long appServiceId, Long userId);


    List<ProjectVO> listProjects(Long organizationId, Long projectId, String params);

    /**
     * 导入内部应用服务
     *
     * @param projectId
     * @param importInternalVOS
     */
    void importAppServiceInternal(Long projectId, List<ApplicationImportInternalVO> importInternalVOS);

    /**
     * 导入内部应用服务（创建gitlabProject）
     *
     * @param appServiceImportPayload
     */
    void importAppServiceGitlab(AppServiceImportPayload appServiceImportPayload);

    void setProjectHook(AppServiceDTO appServiceDTO, Integer projectId, String token, Integer userId);

    void baseCheckApp(Long projectId, Long appServiceId);

    AppServiceDTO baseUpdate(AppServiceDTO appServiceDTO);

    void updateApplicationStatus(AppServiceDTO appServiceDTO);

    AppServiceDTO baseQuery(Long appServiceId);

    PageInfo<AppServiceDTO> basePageByOptions(Long projectId, Boolean isActive, Boolean hasVersion, Boolean
            appMarket,
                                              String type, Boolean doPage, PageRequest pageRequest, String params);

    PageInfo<AppServiceDTO> basePageCodeRepository(Long projectId, PageRequest pageRequest, String params,
                                                   Boolean isProjectOwner, Long userId);


    AppServiceDTO baseQueryByCode(String code, Long appId);

    AppServiceDTO baseQueryByCodeWithNullProject(String code);

    List<AppServiceDTO> baseListByEnvId(Long projectId, Long envId, String status);

    PageInfo<AppServiceDTO> basePageByEnvId(Long projectId, Long envId, Long appServiceId, PageRequest pageRequest);

    List<AppServiceDTO> baseListByActive(Long projectId);

    List<AppServiceDTO> baseListDeployedApp(Long projectId);

    PageInfo<AppServiceDTO> basePageByActiveAndPubAndHasVersion(Long projectId, Boolean isActive,
                                                                PageRequest pageRequest, String params);

    AppServiceDTO baseQueryByToken(String token);

    void baseCheckAppCanDisable(Long applicationId);

    List<AppServiceDTO> baseListByCode(String code);

    List<AppServiceDTO> baseListByGitLabProjectIds(List<Long> gitLabProjectIds);

    void baseDelete(Long appServiceId);

    List<AppServiceDTO> baseListByProjectIdAndSkipCheck(Long projectId);

    List<AppServiceDTO> baseListByProjectId(Long projectId);

    void baseUpdateHarborConfig(Long projectId, Long newConfigId, Long oldConfigId, boolean harborPrivate);

    AppServiceDTO getApplicationServiceDTO(Long projectId, AppServiceReqVO applicationReqDTO);

    AppServiceDTO baseCreate(AppServiceDTO appServiceDTO);

    /**
     * 查询组织共享和市场下载的应用服务并分组返回
     *
     * @return List<AppServiceGroupVO>
     */
    List<AppServiceGroupInfoVO> listAppServiceGroup(Long projectId, Boolean share, String param);

    /**
     * 查询所有应用服务
     *
     * @param projectId
     * @param type      normal_service(本项目服务) share_service(组织下共享) market_service(应用市场下载)
     * @param param
     * @return
     */
    List<AppServiceGroupVO> listAllAppServices(Long projectId, String type, String param, Boolean deployOnly, String serviceType);

    String getToken(Integer gitlabProjectId, String applicationDir, UserAttrDTO userAttrDTO);
}
