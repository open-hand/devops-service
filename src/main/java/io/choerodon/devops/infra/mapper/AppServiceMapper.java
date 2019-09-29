package io.choerodon.devops.infra.mapper;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.infra.dto.AppServiceDTO;
import io.choerodon.mybatis.common.Mapper;

/**
 * Created by younger on 2018/3/28.
 */
public interface AppServiceMapper extends Mapper<AppServiceDTO> {
    void updateByIdSelectiveWithoutAudit(@Param("appService") AppServiceDTO appService);

    List<AppServiceDTO> list(@Param("projectId") Long projectId,
                             @Param("isActive") Boolean isActive,
                             @Param("hasVersion") Boolean hasVersion,
                             @Param("type") String type,
                             @Param("searchParam") Map<String, Object> searchParam,
                             @Param("params") List<String> params,
                             @Param("index") String index);

    List<AppServiceDTO> listByProjectId(@Param("projectId") Long projectId,
                                        @Param("searchParam") Map<String, Object> searchParam,
                                        @Param("params") List<String> params);

    List<AppServiceDTO> listCodeRepository(@Param("projectId") Long projectId,
                                           @Param("searchParam") Map<String, Object> searchParam,
                                           @Param("params") List<String> param,
                                           @Param("isProjectOwner") Boolean isProjectOwner,
                                           @Param("userId") Long userId);

    List<AppServiceDTO> listByEnvId(@Param("projectId") Long projectId,
                                    @Param("envId") Long envId,
                                    @Param("appServiceId") Long appServiceId,
                                    @Param("status") String status);

    List<AppServiceDTO> basePageByActiveAndPubAndHasVersion(@Param("projectId") Long projectId,
                                                            @Param("active") Boolean active,
                                                            @Param("searchParam") Map<String, Object> searchParam,
                                                            @Param("params") List<String> params);

    AppServiceDTO queryByToken(@Param("token") String token);

    List<AppServiceDTO> listByActive(@Param("projectId") Long projectId);

    Integer countByActive(@Param("projectId") Long projectId);

    List<AppServiceDTO> listDeployedApp(@Param("projectId") Long projectId);

    List<AppServiceDTO> listByCode(@Param("code") String code);

    AppServiceDTO queryByCodeWithNoProject(@Param("code") String code);

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

    void updateHarborConfigNullByConfigId(@Param("harborConfigId") Long harborConfigId);

    void updateChartConfigNullByConfigId(@Param("chartConfigId") Long chartConfigId);

    void updateHarborConfigNullByServiceId(@Param("appServiceId") Long appServiceId);

    void updateChartConfigNullByServiceId(@Param("appServiceId") Long appServiceId);

    List<AppServiceDTO> queryOrganizationShareApps(@Param("projectIds") List<Long> projectIds,
                                                   @Param("param") String param,
                                                   @Param("searchProjectId") Long searchProjectId);

    List<AppServiceDTO> queryMarketDownloadApps(@Param("type") String type,
                                                @Param("param") String param,
                                                @Param("appServiceIds") List<Long> appServiceIds,
                                                @Param("searchProjectId") Long searchProjectId);

    /**
     * 根据ProjectID 查询可用的项目共享Apps
     *
     * @param projectId
     * @return
     */
    List<AppServiceDTO> listShareProjectApps(@Param("projectId") Long projectId,
                                             @Param("param") String param,
                                             @Param("searchProjectId") Long searchProjectId);

    List<AppServiceDTO> listProjectMembersAppService(@Param("projectId") Long projectId,
                                                     @Param("isActive") Boolean isActive,
                                                     @Param("hasVersion") Boolean hasVersion,
                                                     @Param("type") String type,
                                                     @Param("searchParam") Map<String, Object> searchParam,
                                                     @Param("params") List<String> params,
                                                     @Param("index") String index,
                                                     @Param("userId") Long userId);


    List<AppServiceDTO> listAppServiceByIds(@Param("ids") Set<Long> ids,
                                            @Param("searchParam") Map<String, Object> searchParam,
                                            @Param("params") List<String> params);

    List<AppServiceDTO> listProjectMembersAppServiceByActive(@Param("projectId") Long projectId,
                                                             @Param("userId") Long userId);

    Integer countProjectMembersAppServiceByActive(@Param("projectId") Long projectId,
                                                             @Param("userId") Long userId);

    List<AppServiceDTO> pageServiceByProjectId(@Param("projectId") Long projectId,
                                               @Param("searchParam") Map<String, Object> searchParam,
                                               @Param("params") List<String> params);

    List<AppServiceDTO> listServiceByVersionIds(@Param("ids") Set<Long> ids);

    int updateIsFailedNullToFalse();

    int updateIsSynchroToTrueWhenFailed();

    int updateIsActiveNullToTrue();

    /**
     * 根据gitlabGroupId和iamUserId获取
     * 在整个项目组用权限的应用对应的gitlabProjectId
     *
     * @return
     */
    List<Long> listGitlabProjectIdByAppPermission(@Param("gitlabGroupId") Long gitlabGroupId,
                                                  @Param("iamUserId") Long iamUserId);
}

