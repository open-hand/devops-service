package io.choerodon.devops.infra.mapper;

import java.util.List;
import java.util.Map;

import io.choerodon.devops.infra.dataobject.ApplicationDO;
import io.choerodon.mybatis.common.BaseMapper;
import org.apache.ibatis.annotations.Param;

/**
 * Created by younger on 2018/3/28.
 */
public interface ApplicationMapper extends BaseMapper<ApplicationDO> {
    List<ApplicationDO> list(@Param("projectId") Long projectId,
                             @Param("isActive") Boolean isActive,
                             @Param("hasVersion") Boolean hasVersion,
                             @Param("type") String type,
                             @Param("searchParam") Map<String, Object> searchParam,
                             @Param("param") String param,
                             @Param("index") String index);

    List<ApplicationDO> listCodeRepository(@Param("projectId") Long projectId,
                                           @Param("searchParam") Map<String, Object> searchParam,
                                           @Param("param") String param,
                                           @Param("isProjectOwner") Boolean isProjectOwner,
                                           @Param("userId") Long userId);

    List<ApplicationDO> listByEnvId(@Param("projectId") Long projectId,
                                    @Param("envId") Long envId,
                                    @Param("appId") Long appId,
                                    @Param("status") String status);

    List<ApplicationDO> listByActiveAndPubAndVersion(@Param("projectId") Long projectId,
                                                     @Param("active") Boolean active,
                                                     @Param("searchParam") Map<String, Object> searchParam,
                                                     @Param("param") String param);

    ApplicationDO queryByToken(@Param("token") String token);

    List<ApplicationDO> listActive(@Param("projectId") Long projectId);

    List<ApplicationDO> listAll(@Param("projectId") Long projectId);

    Integer checkAppCanDisable(@Param("applicationId") Long applicationId);

    List<ApplicationDO> listByCode(@Param("code") String code);

    List<ApplicationDO> listByGitLabProjectIds(@Param("gitlabProjectIds") List<Long> gitlabProjectIds);

    void updateAppToSuccess(@Param("appId") Long appId);

    void updateAppHarborConfig(@Param("projectId") Long projectId,@Param("newConfigId") Long newConfigId, @Param("oldConfigId") Long oldConfigId,@Param("harborPrivate") boolean harborPrivate);
}
