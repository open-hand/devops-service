package io.choerodon.devops.infra.mapper;

import org.apache.ibatis.annotations.Param;

import java.util.*;

import io.choerodon.devops.api.vo.AppServiceRepVO;
import io.choerodon.devops.api.vo.AppServiceSimpleVO;
import io.choerodon.devops.api.vo.LatestAppServiceVO;
import io.choerodon.devops.api.vo.ProjectAppSvcCountVO;
import io.choerodon.devops.api.vo.iam.ResourceVO;
import io.choerodon.devops.infra.dto.AppServiceDTO;
import io.choerodon.mybatis.common.BaseMapper;

/**
 * Created by younger on 2018/3/28.
 */
public interface AppServiceMapper extends BaseMapper<AppServiceDTO> {
    void updateByIdSelectiveWithoutAudit(@Param("appService") AppServiceDTO appService);

    List<AppServiceDTO> list(@Param("projectId") Long projectId,
                             @Param("isActive") Boolean isActive,
                             @Param("hasVersion") Boolean hasVersion,
                             @Param("type") String type,
                             @Param("searchParam") Map<String, Object> searchParam,
                             @Param("params") List<String> params,
                             @Param("index") String index,
                             @Param("includeExternal") Boolean includeExternal,
                             @Param("excludeFailed") Boolean excludeFailed);

    List<AppServiceDTO> listByProjectId(@Param("projectId") Long projectId,
                                        @Param("searchParam") Map<String, Object> searchParam,
                                        @Param("params") List<String> params);

    List<AppServiceDTO> listByEnvId(@Param("projectId") Long projectId,
                                    @Param("envId") Long envId,
                                    @Param("appServiceId") Long appServiceId,
                                    @Param("status") String status);

    AppServiceDTO queryByToken(@Param("token") String token);

    List<AppServiceDTO> listByActive(@Param("projectId") Long projectId, @Param("param") String param);

    List<AppServiceDTO> listByActiveOrderByTargetAppServiceId(@Param("projectId") Long projectId, @Param("targetAppServiceId") Long targetAppServiceId, @Param("param") String param);

    Integer countByActive(@Param("projectId") Long projectId);

    List<AppServiceDTO> listDeployedApp(@Param("projectId") Long projectId);

    List<AppServiceDTO> listByCode(@Param("code") String code);

    List<AppServiceDTO> listByGitLabProjectIds(@Param("gitlabProjectIds") List<Long> gitlabProjectIds);

    void updateAppToSuccess(@Param("appServiceId") Long appServiceId);

    void updateApplicationStatus(@Param("appServiceId") Long appServiceId,
                                 @Param("token") String token,
                                 @Param("gitlabProjectId") Integer gitlabProjectId,
                                 @Param("hookId") Long hookId,
                                 @Param("isSynchro") Boolean isSynchro);

    void updateHarborConfig(@Param("projectId") Long projectId,
                            @Param("newConfigId") Long newConfigId,
                            @Param("oldConfigId") Long oldConfigId,
                            @Param("harborPrivate") boolean harborPrivate);


    List<AppServiceDTO> listShareApplicationService(@Param("appServiceIds") List<Long> appServiceIds,
                                                    @Param("projectId") Long projectId,
                                                    @Param("type") String type,
                                                    @Param("params") List<String> params);

    /**
     * 查出含有版本的共享应用服务
     *
     * @param projectIds       本组织下的其他项目id
     * @param currentProjectId 当前项目的id
     * @return 应用服务列表
     */
    List<AppServiceDTO> listShareAppServiceHavingVersion(@Param("projectIds") Collection<Long> projectIds,
                                                         @Param("currentProjectId") Long currentProjectId,
                                                         @Param("type") String type,
                                                         @Param("params") List<String> params,
                                                         @Param("includeExternal") Boolean includeExternal);

    void updateHarborConfigNullByConfigId(@Param("harborConfigId") Long harborConfigId);

    void updateChartConfigNullByConfigId(@Param("chartConfigId") Long chartConfigId);

    void updateHarborConfigNullByServiceId(@Param("appServiceId") Long appServiceId);

    void updateChartConfigNullByServiceId(@Param("appServiceId") Long appServiceId);

    List<AppServiceDTO> queryOrganizationShareApps(@Param("projectIds") List<Long> projectIds,
                                                   @Param("param") String param,
                                                   @Param("projectId") Long projectId,
                                                   @Param("includeExternal") Boolean includeExternal);


    List<AppServiceDTO> listProjectMembersAppService(@Param("projectId") Long projectId,
                                                     @Param("appServiceIds") Set<Long> appServiceIds,
                                                     @Param("isActive") Boolean isActive,
                                                     @Param("hasVersion") Boolean hasVersion,
                                                     @Param("type") String type,
                                                     @Param("searchParam") Map<String, Object> searchParam,
                                                     @Param("params") List<String> params,
                                                     @Param("doSort") Boolean doSort,
                                                     @Param("userId") Long userId,
                                                     @Param("includeExternal") Boolean includeExternal,
                                                     @Param("excludeFailed") Boolean excludeFailed);


    List<AppServiceDTO> listAppServiceByIds(@Param("ids") Set<Long> ids,
                                            @Param("searchParam") Map<String, Object> searchParam,
                                            @Param("params") List<String> params);

    List<AppServiceDTO> listProjectMembersAppServiceByActive(@Param("projectId") Long projectId,
                                                             @Param("appServiceIds") Set<Long> appServiceIds,
                                                             @Param("userId") Long userId,
                                                             @Param("param") String param);

    List<AppServiceDTO> listProjectMembersAppServiceByActiveOrderByTargetAppServiceId(@Param("projectId") Long projectId,
                                                                                      @Param("targetAppServiceId") Long targetAppServiceId,
                                                                                      @Param("appServiceIds") Set<Long> appServiceIds,
                                                                                      @Param("userId") Long userId,
                                                                                      @Param("param") String param);

    Integer countProjectMembersAppServiceByActive(@Param("projectId") Long projectId,
                                                  @Param("appServiceIds") Set<Long> appServiceIds,
                                                  @Param("userId") Long userId);

    List<AppServiceDTO> pageServiceByProjectId(@Param("projectId") Long projectId,
                                               @Param("searchParam") Map<String, Object> searchParam,
                                               @Param("params") List<String> params);

    List<AppServiceDTO> listServiceByVersionIds(@Param("ids") Set<Long> ids);

    int updateIsFailedNullToFalse();

    int updateIsSynchroToTrueWhenFailed();

    List<Long> listAllAppServiceIds(@Param("projectId") Long projectId);

    Set<Long> listAllExternalAppServiceIds(@Param("projectId") Long projectId);

    int updateIsActiveNullToTrue();

    List<AppServiceDTO> listAll(@Param("projectId") Long projectId, @Param("envId") Long envId, @Param("appServiceName") String appServiceName);

    List<AppServiceDTO> queryAppServicesHavingVersions(@Param("projectId") Long projectId);

    /**
     * 作为所有者，查询能够用于创建CI流水线的应用服务列表，限制20条
     *
     * @param projectId   项目id
     * @param searchParam 字段模糊搜索参数
     * @param params      整体模糊搜索参数
     * @return 列表
     */
    List<AppServiceDTO> listAppServiceToCreatePipelineForOwner(@Param("projectId") Long projectId,
                                                               @Param("searchParam") Map<String, Object> searchParam,
                                                               @Param("params") List<String> params);

    /**
     * 作为项目成员，查询能够用于创建CI流水线的应用服务列表，限制20条
     *
     * @param projectId   项目id
     * @param iamUserId   用户id
     * @param searchParam 字段模糊搜索参数
     * @param params      整体模糊搜索参数
     * @return 列表
     */
    List<AppServiceDTO> listAppServiceToCreatePipelineForMember(@Param("projectId") Long projectId,
                                                                @Param("iamUserId") Long iamUserId,
                                                                @Param("appServiceIds") Set<Long> appServiceIds,
                                                                @Param("searchParam") Map<String, Object> searchParam,
                                                                @Param("params") List<String> params);

    /**
     * 查询出指定项目下最近使用过的应用
     *
     * @param projectIds 项目ids
     * @param time
     * @return 应用id和最后更新时间
     */
    List<LatestAppServiceVO> listLatestUseAppServiceIdAndDate(@Param("projectIds") List<Long> projectIds,
                                                              @Param("userId") Long userId,
                                                              @Param("time") Date time);

    List<AppServiceDTO> listByActiveAndProjects(@Param("projectIds") List<Long> projectIds);

    List<ProjectAppSvcCountVO> countByProjectIds(@Param("projectIds") List<Long> projectIds);


    List<AppServiceDTO> listAppServiceByIdsWithParam(@Param("appServiceIds") List<Long> appServiceIds, @Param("param") String param);

    AppServiceDTO selectWithEmptyRepositoryByPrimaryKey(Long appServiceId);

    /**
     * 查询在这个项目下且有指定的这个版本的，且code符合的应用服务列表
     *
     * @param projectIds     项目id列表
     * @param appServiceCode 应用服务code
     * @param version        应用服务版本
     * @return 应用服务列表
     */
    List<AppServiceDTO> inProjectsAndHavingVersion(@Param("projectIds") Set<Long> projectIds, @Param("appServiceCode") String appServiceCode, @Param("version") String version);

    ResourceVO queryResourceById(@Param("projectId") Long projectId);

    void updatePomFields(@Param("id") Long id,
                         @Param("groupId") String groupId,
                         @Param("artifactId") String artifactId);

    List<AppServiceSimpleVO> listByProjectIdsAndCodes(@Param("projectIds") List<Long> projectIds, @Param("codes") List<String> codes);

    List<AppServiceRepVO> queryApplicationCenter(@Param("projectId") Long projectId, @Param("envId") Long envId, @Param("type") String type, @Param("params") String params);

    Set<Long> listAllIdsByProjectId(@Param("projectId") Long projectId);

    List<Long> listProjectIdsByAppIds(@Param("appIds") List<Long> appIds);

    AppServiceDTO queryByPipelineId(@Param("pipelineId") Long pipelineId);
}

