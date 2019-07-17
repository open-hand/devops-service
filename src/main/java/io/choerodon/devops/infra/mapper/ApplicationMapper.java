package io.choerodon.devops.infra.mapper;

import java.util.List;
import java.util.Map;

import io.choerodon.devops.infra.dto.ApplicationDTO;
import io.choerodon.mybatis.common.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * Created by younger on 2018/3/28.
 */
public interface ApplicationMapper extends Mapper<ApplicationDTO> {
    List<ApplicationDTO> list(@Param("projectId") Long projectId,
                              @Param("isActive") Boolean isActive,
                              @Param("hasVersion") Boolean hasVersion,
                              @Param("appMarket") Boolean appMarket,
                              @Param("type") String type,
                              @Param("searchParam") Map<String, Object> searchParam,
                              @Param("param") String param,
                              @Param("index") String index);

    List<ApplicationDTO> listAll(@Param("projectId") Long projectId);

    List<ApplicationDTO> listCodeRepository(@Param("projectId") Long projectId,
                                            @Param("searchParam") Map<String, Object> searchParam,
                                            @Param("param") String param,
                                            @Param("isProjectOwner") Boolean isProjectOwner,
                                            @Param("userId") Long userId);

    List<ApplicationDTO> listByEnvId(@Param("projectId") Long projectId,
                                     @Param("envId") Long envId,
                                     @Param("appId") Long appId,
                                     @Param("status") String status);

    List<ApplicationDTO> basePageByActiveAndPubAndHasVersion(@Param("projectId") Long projectId,
                                                             @Param("active") Boolean active,
                                                             @Param("searchParam") Map<String, Object> searchParam,
                                                             @Param("param") String param);

    ApplicationDTO queryByToken(@Param("token") String token);

    List<ApplicationDTO> listByActive(@Param("projectId") Long projectId);

    List<ApplicationDTO> listDeployedApp(@Param("projectId") Long projectId);

    Integer checkAppCanDisable(@Param("applicationId") Long applicationId);

    List<ApplicationDTO> listByCode(@Param("code") String code);

    ApplicationDTO queryByCodeWithNoProject(@Param("code") String code);

    List<ApplicationDTO> listByGitLabProjectIds(@Param("gitlabProjectIds") List<Long> gitlabProjectIds);

    void updateAppToSuccess(@Param("appId") Long appId);

    void updateApplicationStatus(@Param("appId") Long appId,
                                 @Param("token") String token,
                                 @Param("gitlabProjectId") Integer gitlabProjectId,
                                 @Param("hookId") Long hookId,
                                 @Param("isSynchro") Boolean isSynchro);

    void updateHarborConfig(@Param("projectId") Long projectId, @Param("newConfigId") Long newConfigId, @Param("oldConfigId") Long oldConfigId, @Param("harborPrivate") boolean harborPrivate);
}
