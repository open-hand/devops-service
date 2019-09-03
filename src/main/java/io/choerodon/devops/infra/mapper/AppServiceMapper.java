package io.choerodon.devops.infra.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.infra.dto.AppServiceDTO;
import io.choerodon.mybatis.common.Mapper;

/**
 * Created by younger on 2018/3/28.
 */
public interface AppServiceMapper extends Mapper<AppServiceDTO> {
    List<AppServiceDTO> list(@Param("appId") Long appId,
                             @Param("isActive") Boolean isActive,
                             @Param("hasVersion") Boolean hasVersion,
                             @Param("type") String type,
                             @Param("searchParam") Map<String, Object> searchParam,
                             @Param("params") List<String> params,
                             @Param("index") String index);

    List<AppServiceDTO> listByAppId(@Param("appId") Long appId,
                                    @Param("searchParam") Map<String, Object> searchParam,
                                    @Param("params") List<String> params);

    List<AppServiceDTO> listAll(@Param("appId") Long appId);

    List<AppServiceDTO> listCodeRepository(@Param("appId") Long appId,
                                           @Param("searchParam") Map<String, Object> searchParam,
                                           @Param("params") List<String> param,
                                           @Param("isProjectOwner") Boolean isProjectOwner,
                                           @Param("userId") Long userId);

    List<AppServiceDTO> listByEnvId(@Param("appId") Long appId,
                                    @Param("envId") Long envId,
                                    @Param("appServiceId") Long appServiceId,
                                    @Param("status") String status);

    List<AppServiceDTO> basePageByActiveAndPubAndHasVersion(@Param("appId") Long appId,
                                                            @Param("active") Boolean active,
                                                            @Param("searchParam") Map<String, Object> searchParam,
                                                            @Param("params") List<String> params);

    AppServiceDTO queryByToken(@Param("token") String token);

    List<AppServiceDTO> listByActive(@Param("appId") Long appId);

    List<AppServiceDTO> listDeployedApp(@Param("appId") Long appId);

    Integer checkAppCanDisable(@Param("appServiceId") Long appServiceId);

    List<AppServiceDTO> listByCode(@Param("code") String code);

    AppServiceDTO queryByCodeWithNoProject(@Param("code") String code);

    List<AppServiceDTO> listByGitLabProjectIds(@Param("gitlabProjectIds") List<Long> gitlabProjectIds);

    void updateAppToSuccess(@Param("appServiceId") Long appServiceId);

    void updateApplicationStatus(@Param("appServiceId") Long appServiceId,
                                 @Param("token") String token,
                                 @Param("gitlabProjectId") Integer gitlabProjectId,
                                 @Param("hookId") Long hookId,
                                 @Param("isSynchro") Boolean isSynchro);

    void updateHarborConfig(@Param("appId") Long appId, @Param("newConfigId") Long newConfigId, @Param("oldConfigId") Long oldConfigId, @Param("harborPrivate") boolean harborPrivate);


    List<AppServiceDTO> listShareApplicationService(@Param("appServiceIds") List<Long> appServiceIds,
                                                    @Param("projectId") Long projectId,
                                                    @Param("type") String type,
                                                    @Param("params") List<String> params);

    void updateHarborConfigNullByConfigId(@Param("harborConfigId") Long harborConfigId);

    void updateChartConfigNullByConfigId(@Param("chartConfigId") Long chartConfigId);

    void updateHarborConfigNullByServiceId(@Param("appServiceId") Long appServiceId);

    void updateChartConfigNullByServiceId(@Param("appServiceId") Long appServiceId);

    List<AppServiceDTO> queryOrganizationShareApps(@Param("appServiceIds") List<Long> appServiceIds);

    List<AppServiceDTO> queryMarketDownloadApps(@Param("type") String type);

    /**
     * 根据ProjectID 查询可用的项目共享Apps
     *
     * @param projectId
     * @return
     */
    List<AppServiceDTO> listShareProjectApps(@Param("projectId") Long projectId);

    List<AppServiceDTO> listProjectMembersAppService(@Param("appId") Long appId,
                                                     @Param("isActive") Boolean isActive,
                                                     @Param("hasVersion") Boolean hasVersion,
                                                     @Param("type") String type,
                                                     @Param("searchParam") Map<String, Object> searchParam,
                                                     @Param("params") List<String> params,
                                                     @Param("index") String index,
                                                     @Param("userId") Long userId);
}

