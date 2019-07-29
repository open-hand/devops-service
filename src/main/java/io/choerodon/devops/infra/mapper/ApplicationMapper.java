package io.choerodon.devops.infra.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import io.choerodon.devops.infra.dto.ApplicationServiceDTO;
import io.choerodon.mybatis.common.Mapper;

/**
 * Created by younger on 2018/3/28.
 */
public interface ApplicationMapper extends Mapper<ApplicationServiceDTO> {
    List<ApplicationServiceDTO> list(@Param("projectId") Long projectId,
                                     @Param("isActive") Boolean isActive,
                                     @Param("hasVersion") Boolean hasVersion,
                                     @Param("appMarket") Boolean appMarket,
                                     @Param("type") String type,
                                     @Param("searchParam") Map<String, Object> searchParam,
                                     @Param("param") String param,
                                     @Param("index") String index);

    List<ApplicationServiceDTO> listAll(@Param("projectId") Long projectId);

    List<ApplicationServiceDTO> listCodeRepository(@Param("projectId") Long projectId,
                                                   @Param("searchParam") Map<String, Object> searchParam,
                                                   @Param("param") String param,
                                                   @Param("isProjectOwner") Boolean isProjectOwner,
                                                   @Param("userId") Long userId);

    List<ApplicationServiceDTO> listByEnvId(@Param("projectId") Long projectId,
                                            @Param("envId") Long envId,
                                            @Param("appId") Long appId,
                                            @Param("status") String status);

    List<ApplicationServiceDTO> basePageByActiveAndPubAndHasVersion(@Param("projectId") Long projectId,
                                                                    @Param("active") Boolean active,
                                                                    @Param("searchParam") Map<String, Object> searchParam,
                                                                    @Param("param") String param);

    ApplicationServiceDTO queryByToken(@Param("token") String token);

    List<ApplicationServiceDTO> listByActive(@Param("projectId") Long projectId);

    List<ApplicationServiceDTO> listDeployedApp(@Param("projectId") Long projectId);

    Integer checkAppCanDisable(@Param("applicationId") Long applicationId);

    List<ApplicationServiceDTO> listByCode(@Param("code") String code);

    ApplicationServiceDTO queryByCodeWithNoProject(@Param("code") String code);

    List<ApplicationServiceDTO> listByGitLabProjectIds(@Param("gitlabProjectIds") List<Long> gitlabProjectIds);

    void updateAppToSuccess(@Param("appId") Long appId);

    void updateApplicationStatus(@Param("appId") Long appId,
                                 @Param("token") String token,
                                 @Param("gitlabProjectId") Integer gitlabProjectId,
                                 @Param("hookId") Long hookId,
                                 @Param("isSynchro") Boolean isSynchro);

    void updateHarborConfig(@Param("projectId") Long projectId, @Param("newConfigId") Long newConfigId, @Param("oldConfigId") Long oldConfigId, @Param("harborPrivate") boolean harborPrivate);


    List<ApplicationServiceDTO> listShareApplications(@Param("organizationId") Long organizationId,
                                                      @Param("projectId") Long projectId,
                                                      @Param("params") String params);
}

