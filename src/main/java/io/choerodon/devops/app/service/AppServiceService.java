package io.choerodon.devops.app.service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;

import io.choerodon.core.domain.Page;
import io.choerodon.devops.api.vo.*;
import io.choerodon.devops.api.vo.iam.ResourceVO;
import io.choerodon.devops.api.vo.open.OpenAppServiceReqVO;
import io.choerodon.devops.app.eventhandler.payload.AppServiceImportPayload;
import io.choerodon.devops.app.eventhandler.payload.DevOpsAppImportServicePayload;
import io.choerodon.devops.app.eventhandler.payload.DevOpsAppServicePayload;
import io.choerodon.devops.infra.dto.AppExternalConfigDTO;
import io.choerodon.devops.infra.dto.AppServiceDTO;
import io.choerodon.devops.infra.dto.UserAttrDTO;
import io.choerodon.devops.infra.dto.harbor.HarborRepoConfigDTO;
import io.choerodon.devops.infra.enums.GitPlatformType;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

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
     * 内部查询项目下所有应用服务 / 不区分权限
     *
     * @param projectId 项目id
     * @param params    查询参数
     * @param pageable  分页参数
     * @return 应用服务列表
     */
    Page<AppServiceRepVO> internalListAllInProject(Long projectId, String params, PageRequest pageable);

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
     * @param projectId       项目id
     * @param isActive        是否启用
     * @param hasVersion      是否存在版本
     * @param pageable        分页参数
     * @param params          参数
     * @param includeExternal
     * @param excludeFailed   排除掉创建失败的应用
     * @return Page
     */
    Page<AppServiceRepVO> pageByOptions(Long projectId,
                                        Boolean isActive,
                                        Boolean hasVersion,
                                        String type,
                                        Boolean doPage,
                                        PageRequest pageable,
                                        SearchVO searchVO,
                                        Boolean checkMember,
                                        Boolean includeExternal,
                                        Boolean excludeFailed);

    /**
     * 处理服务创建逻辑
     *
     * @param gitlabProjectEventDTO 服务信息
     */
    void operationApplication(DevOpsAppServicePayload gitlabProjectEventDTO);

    void operationExternalApplication(DevOpsAppServicePayload gitlabProjectEventDTO);

    /**
     * 处理服务导入逻辑
     *
     * @param devOpsAppImportPayload 服务导入相关信息
     */
    void operationAppServiceImport(DevOpsAppImportServicePayload devOpsAppImportPayload);


    /**
     * 设置服务创建失败状态
     *
     * @param input        服务信息
     * @param projectId    项目id
     * @param appServiceId 应用服务id
     */
    void setAppErrStatus(String input, Long projectId, Long appServiceId);

    /**
     * 项目下服务查询ci脚本文件
     *
     * @param token token
     * @return File
     */
    String queryFile(String token);

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
     * @param projectId 项目id
     * @param envId     环境Id
     * @param pageable  分页参数
     * @return baseList of ApplicationRepDTO
     */
    Page<AppServiceCodeVO> pageByIds(Long projectId, Long envId, Long appServiceId, PageRequest pageable);

    Page<AppServiceRepVO> pageInternalByOptionsWithAccessLevel(Long projectId,
                                                               PageRequest pageable,
                                                               SearchVO searchVO);

    /**
     * 项目下查询所有已经启用的服务
     *
     * @param projectId 项目id
     * @return baseList of ApplicationRepDTO
     */
    List<AppServiceRepVO> listByActive(Long projectId);

    /**
     * 项目下查询所有已经启用服务数量
     */

    Integer countByActive(Long projectId);

    /**
     * 项目下查询所有可选已经启用的服务
     *
     * @param projectId 项目id
     * @param envId     环境id
     * @return baseList of ApplicationRepDTO
     */
    List<AppServiceRepVO> listAll(Long projectId, Long envId);

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
     * 创建服务判断名称是否存在
     *
     * @param projectId 项目Id
     * @param name      服务name
     * @return true表示通过
     */
    boolean isNameUnique(Long projectId, String name);

    /**
     * 创建服务判断编码是否存在
     *
     * @param projectId 项目id
     * @param code      服务code
     * @return true表示通过
     */
    boolean isCodeUnique(Long projectId, String code);

    /**
     * 批量校验应用服务code和name
     *
     * @param projectId
     * @param appServiceBatchCheckVO
     * @return
     */
    AppServiceBatchCheckVO checkCodeByProjectId(Long projectId, AppServiceBatchCheckVO appServiceBatchCheckVO);

    /**
     * valid the repository url and access token
     *
     * @param gitPlatformType git platform type
     * @param repositoryUrl   repository url
     * @param accessToken     access token (Nullable)
     * @return true if valid
     */
    Boolean validateRepositoryUrlAndToken(GitPlatformType gitPlatformType, String repositoryUrl, String accessToken);

    Boolean validateRepositoryUrlAndUsernameAndPassword(String repositoryUrl, String username, String password);

    /**
     * 从外部代码托管平台导入项目创建服务
     *
     * @param projectId          project id
     * @param appServiceImportVO 导入操作的相关信息
     * @return response
     */
    AppServiceRepVO importApp(Long projectId, AppServiceImportVO appServiceImportVO, Boolean isTemplate);

    /**
     * 项目下从通用git导入服务
     *
     * @param projectId          project id
     * @param appServiceImportVO 导入操作的相关信息
     * @return response
     */
    AppServiceRepVO importFromGeneralGit(Long projectId, AppServiceImportVO appServiceImportVO);

    /**
     * 根据服务code查询服务
     *
     * @param projectId 项目Id
     * @param code      服务code
     * @return ApplicationRepDTO
     */
    AppServiceRepVO queryByCode(Long projectId, String code);

    /**
     * 校验chart配置信息是否正确
     *
     * @param url      ChartMuseum地址
     * @param username 用户名
     * @param password 密码
     * @return true如果通过 (未通过则抛出错误信息)
     */
    Boolean checkChartOnOrganization(String url, @Nullable String username, @Nullable String password);

    /**
     * 项目层或应用层校验chart配置信息是否正确
     *
     * @param projectId
     * @param url       ChartMuseum地址
     * @param username  用户名
     * @param password  密码
     * @return CheckInfoVO
     */
    CheckInfoVO checkChart(Long projectId, String url, @Nullable String username, @Nullable String password);

    /**
     * 查看sonarqube相关信息
     *
     * @param projectId    项目Id
     * @param appServiceId 服务id
     * @return 信息
     */
    SonarContentsVO getSonarContent(Long projectId, Long appServiceId);

    /**
     * 查看sonarqube相关报表
     *
     * @param projectId    项目Id
     * @param appServiceId 服务id
     * @return 报表
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
     * @param pageable
     * @param params
     * @return
     */
    Page<AppServiceRepVO> pageShareAppService(Long projectId, boolean doPage, PageRequest pageable, String params);

    /**
     * 根据appServiceId查询服务服务有权限的项目成员和项目所有者
     *
     * @param projectId
     * @param appServiceId
     * @param pageable
     * @param searchParam
     */
    Page<DevopsUserPermissionVO> pagePermissionUsers(Long projectId, Long appServiceId, PageRequest pageable, String searchParam);

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

    /**
     * 查询项目成员 项目下有权限的应用服务Id
     *
     * @param organizationId
     * @param projectId
     * @param userId
     * @return
     */
    Set<Long> getMemberAppServiceIds(Long organizationId, Long projectId, Long userId);


    void baseCheckApp(Long projectId, Long appServiceId);

    AppServiceDTO baseUpdate(AppServiceDTO appServiceDTO);

    AppServiceDTO baseQuery(Long appServiceId);

    Page<AppServiceDTO> basePageByOptions(Long projectId,
                                          Boolean isActive,
                                          Boolean hasVersion,
                                          String type,
                                          Boolean doPage,
                                          PageRequest pageable,
                                          SearchVO searchVO,
                                          Boolean checkMember,
                                          Boolean includeExternal,
                                          Boolean excludeFailed);

    AppServiceDTO baseQueryByCode(String code, Long projectId);

    List<AppServiceDTO> baseListByEnvId(Long projectId, Long envId, String status);

    Page<AppServiceDTO> basePageByEnvId(Long projectId, Long envId, Long appServiceId, PageRequest pageable);

    AppServiceDTO baseQueryByToken(String token);

    AppServiceDTO queryByTokenOrThrowE(String token);

    void baseDelete(Long appServiceId);

    List<AppServiceDTO> baseListByProjectId(Long projectId);

    AppServiceDTO getApplicationServiceDTO(Long projectId, AppServiceReqVO applicationReqDTO);

    AppServiceDTO baseCreate(AppServiceDTO appServiceDTO);

    /**
     * 导入应用下根据组织共享或者市场下载的查询应用服务
     * // 2020年05月14日19:20:34 更正： 市场逻辑去掉之后，应该mode只能是share了, 也就是share=true
     *
     * @return List<AppServiceGroupVO>
     */
    Page<AppServiceGroupInfoVO> pageAppServiceByMode(Long projectId, Boolean share, Long searchProjectId, String param, Boolean includeExternal, PageRequest pageable);

    /**
     * 查询所有应用服务
     *
     * @param projectId
     * @param type      normal_service(本项目服务) share_service(组织下共享) market_service(应用市场下载)
     * @param param
     * @return
     */
    List<AppServiceGroupVO> listAllAppServices(Long projectId, String type, String param, String serviceType, Long appServiceId, Boolean includeExternal);

    String getToken(Integer gitlabProjectId, String applicationDir, UserAttrDTO userAttrDTO);

    AppServiceDTO queryByGitlabProjectId(Long gitlabProjectId);

    /**
     * 查询单个项目下的应用服务
     *
     * @param projectId
     * @return
     */
    Page<AppServiceVO> listAppByProjectId(Long projectId, Boolean doPage, PageRequest pageable, String params);

    /**
     * 批量查询应用服务
     *
     * @param projectId    项目id
     * @param ids          应用服务id
     * @param doPage       是否分页
     * @param withVersions 是否需要版本信息
     * @param pageable     分页参数
     * @param params       查询参数
     * @return 应用服务信息
     */
    Page<AppServiceVO> listAppServiceByIds(Long projectId, Set<Long> ids, Boolean doPage, boolean withVersions, PageRequest pageable, String params);


    /**
     * 批量查询应用服务
     *
     * @param ids      应用服务id
     * @param doPage   是否分页
     * @param pageable 分页参数
     * @param params   查询参数
     * @return 应用服务信息
     */
    Page<AppServiceRepVO> listAppServiceByIds(Set<Long> ids, Boolean doPage, PageRequest pageable, String params);

    /**
     * 通过一组id分页查询或者不传id时进行分页查询
     *
     * @param projectId 项目id
     * @param ids       应用服务Id
     * @param doPage    是否分页
     * @param pageable  分页参数
     * @return 结果
     */
    Page<AppServiceVO> listByIdsOrPage(Long projectId, @Nullable Set<Long> ids, @Nullable Boolean doPage, PageRequest pageable, String params);

    /**
     * 根据导入应用类型查询应用所属的项目集合
     *
     * @param share
     * @return
     */
    List<ProjectVO> listProjectByShare(Long projectId, Boolean share, Boolean includeExternal);

    /**
     * 根据版本Id集合查询应用服务
     *
     * @param ids
     * @return
     */
    List<AppServiceVO> listServiceByVersionIds(Set<Long> ids);

    void replaceParams(Long projectId, String newServiceCode,
                       String newGroupName,
                       String applicationDir,
                       String oldServiceCode,
                       String oldGroupName,
                       Boolean isGetWorkingDirectory);

    String checkAppServiceType(Long projectId, @Nullable Long appServiceProjectId, String source);

    void deleteAppServiceSage(Long projectId, Long appServiceId);

    List<AppServiceTemplateVO> listServiceTemplates();

    AppServiceMsgVO checkAppService(Long projectId, Long appServiceId);

    /**
     * 列出项目下有版本的普通应用服务，任何角色可以查到所有的的应用服务
     * 无论有没有权限
     *
     * @param projectId 项目id
     * @return 有版本的应用服务列表
     */
    List<AppServiceSimpleVO> listAppServiceHavingVersions(Long projectId);

    Map<Long, Integer> countByProjectId(List<Long> longList);

    /**
     * 判断项目下是否还能创建应用服务
     *
     * @param projectId 项目id
     */
    Boolean checkEnableCreateAppSvc(Long projectId);

    /**
     * 分页查询用于创建CI流水线的应用服务
     * 1. 默认查询20条
     * 2. 要用户有权限的
     * 3. 要创建成功且启用的
     * 4. 要能够模糊搜索
     * 5. 不能查出已经有流水线的
     * 6. 要有master分支的
     *
     * @param projectId   项目id
     * @param pageRequest 分页参数
     * @param params      查询参数，用于搜索
     * @return 应用服务列表
     */
    Page<AppServiceSimpleVO> pageAppServiceToCreateCiPipeline(Long projectId, PageRequest pageRequest, @Nullable String params);

    /**
     * 查出不带.git后缀的gitlab仓库地址
     *
     * @param appServiceId 应用服务id
     * @return 地址
     */
    String calculateGitlabProjectUrlWithSuffix(Long appServiceId);

    /**
     * 查询其他项目应用服务信息
     *
     * @param projectId
     * @param appServiceId
     * @return
     */
    AppServiceRepVO queryOtherProjectAppServiceWithRepositoryInfo(Long projectId, Long appServiceId);

    /**
     * 列举出同一组织下其他项目的应用服务(每个项目最多取5条)
     *
     * @param projectId
     * @return
     */
    Page<AppServiceUnderOrgVO> listAppServiceUnderOrg(Long projectId, Long appServiceId, String searchParam, PageRequest pageRequest);

    /**
     * 查看sonarqube相关信息
     *
     * @param projectId    项目Id
     * @param appServiceId 服务id
     * @return 信息
     */
    SonarContentsVO getSonarContentFromCache(Long projectId, Long appServiceId);

    List<AppServiceDTO> baseListByIds(Set<Long> appServiceIds);

    List<AppServiceImportPayload> createAppService(Long projectId, List<ApplicationImportInternalVO> importInternalVOS);

    void importMarketAppServiceGitlab(AppServiceImportPayload appServiceImportPayload);

    /**
     * 查询项目下资源使用情况
     *
     * @param projectIds
     * @return
     */
    List<ResourceVO> listResourceByIds(Long organizationId, List<Long> projectIds);

    /**
     * @param appServiceList
     * @return
     */
    List<AppServiceSimpleVO> listByProjectIdAndCode(List<AppServiceSimpleVO> appServiceList);

    Long countAppCountByOptions(Long projectId);

    Page<AppServiceRepVO> applicationCenter(Long projectId, Long envId, String type, String params, PageRequest pageRequest);

    List<DevopsEnvironmentRepVO> listEnvByAppServiceId(Long projectId, Long appServiceId);

    Boolean checkDeleteEnvApp(Long appServiceId, Long envId);

    Set<Long> getMemberAppServiceIdsByAccessLevel(Long organizationId, Long projectId, Long userId, Integer value, Long appId);

    void batchTransfer(Long projectId, List<AppServiceTransferVO> appServiceTransferVOList);

    void createAppServiceForTransfer(AppServiceTransferVO appServiceTransferVO);

    List<CheckAppServiceCodeAndNameVO> checkNameAndCode(Long projectId, List<CheckAppServiceCodeAndNameVO> codeAndNameVOList);

    OpenAppServiceReqVO openCreateAppService(Long projectId, OpenAppServiceReqVO openAppServiceReqVO);

    String getPrivateToken(Long projectId, String serviceCode, String email);

    String getSshUrl(Long projectId, String orgCode, String projectCode, String serviceCode);

    AppServiceDTO createExternalApp(Long projectId, ExternalAppServiceVO externalAppServiceVO);

    Boolean isExternalGitlabUrlUnique(String externalGitlabUrl);

    Boolean testConnection(AppExternalConfigDTO appExternalConfigDTO);

    Set<Long> listExternalAppIdByProjectId(Long projectId);

    List<AppServiceDTO> queryAppByProjectIds(List<Long> projectIds);

    Page<AppServiceVO> pageByActive(Long projectId, Long targetProjectId, Long targetAppServiceId, PageRequest pageRequest, String param);

    Set<Long> listAllIdsByProjectId(Long projectId);

    HarborRepoConfigDTO queryRepoConfigById(Long projectId, Long appServiceId);

    /**
     * 根据应用id列出所有应用所在的项目id
     *
     * @param appIds
     * @return
     */
    List<Long> listProjectIdsByAppIds(List<Long> appIds);

    ImageRepoInfoVO queryRepoConfigByCode(Long projectId, String code, String repoType, String repoCode);

    AppServiceDTO queryByPipelineId(Long pipelineId);
}
